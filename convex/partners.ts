import { mutation, query } from "./_generated/server";
import { v } from "convex/values";

/**
 * Look up a user by friend code.
 */
export const lookupByFriendCode = query({
  args: { code: v.string() },
  handler: async (ctx, { code }) => {
    const identity = await ctx.auth.getUserIdentity();
    if (!identity) return null;

    const friendCode = await ctx.db
      .query("friendCodes")
      .withIndex("by_code", (q) => q.eq("code", code.toUpperCase()))
      .unique();

    if (!friendCode) return null;

    const user = await ctx.db.get(friendCode.userId);
    if (!user) return null;

    return {
      userId: user._id,
      displayName: user.displayName,
    };
  },
});

/**
 * Send a partner request by userId.
 */
export const sendRequest = mutation({
  args: { partnerId: v.id("users") },
  handler: async (ctx, { partnerId }) => {
    const identity = await ctx.auth.getUserIdentity();
    if (!identity) throw new Error("Not authenticated");

    const user = await ctx.db
      .query("users")
      .withIndex("by_externalId", (q) => q.eq("externalId", identity.subject))
      .unique();
    if (!user) throw new Error("User not found");
    if (user._id === partnerId) throw new Error("Cannot partner with yourself");

    // Check for existing request in either direction
    const existingForward = await ctx.db
      .query("partnerships")
      .withIndex("by_pair", (q) =>
        q.eq("requesterId", user._id).eq("partnerId", partnerId)
      )
      .unique();
    if (existingForward) throw new Error("Request already exists");

    const existingReverse = await ctx.db
      .query("partnerships")
      .withIndex("by_pair", (q) =>
        q.eq("requesterId", partnerId).eq("partnerId", user._id)
      )
      .unique();
    if (existingReverse) throw new Error("Request already exists");

    await ctx.db.insert("partnerships", {
      requesterId: user._id,
      partnerId,
      status: "pending",
      createdAt: Date.now(),
    });
  },
});

/**
 * Accept or reject a partner request.
 */
export const respondToRequest = mutation({
  args: {
    partnershipId: v.id("partnerships"),
    accept: v.boolean(),
  },
  handler: async (ctx, { partnershipId, accept }) => {
    const identity = await ctx.auth.getUserIdentity();
    if (!identity) throw new Error("Not authenticated");

    const user = await ctx.db
      .query("users")
      .withIndex("by_externalId", (q) => q.eq("externalId", identity.subject))
      .unique();
    if (!user) throw new Error("User not found");

    const partnership = await ctx.db.get(partnershipId);
    if (!partnership) throw new Error("Partnership not found");
    if (partnership.partnerId !== user._id)
      throw new Error("Not your request to respond to");
    if (partnership.status !== "pending")
      throw new Error("Already responded");

    await ctx.db.patch(partnershipId, {
      status: accept ? "accepted" : "rejected",
      respondedAt: Date.now(),
    });
  },
});

/**
 * Get all partnerships for the current user (both directions).
 */
export const getMyPartners = query({
  args: {},
  handler: async (ctx) => {
    const identity = await ctx.auth.getUserIdentity();
    if (!identity) return [];

    const user = await ctx.db
      .query("users")
      .withIndex("by_externalId", (q) => q.eq("externalId", identity.subject))
      .unique();
    if (!user) return [];

    const asRequester = await ctx.db
      .query("partnerships")
      .withIndex("by_requester", (q) => q.eq("requesterId", user._id))
      .collect();

    const asPartner = await ctx.db
      .query("partnerships")
      .withIndex("by_partner", (q) => q.eq("partnerId", user._id))
      .collect();

    const all = [...asRequester, ...asPartner];

    const result = await Promise.all(
      all.map(async (p) => {
        const otherUserId =
          p.requesterId === user._id ? p.partnerId : p.requesterId;
        const otherUser = await ctx.db.get(otherUserId);
        return {
          partnershipId: p._id,
          otherUserId,
          otherDisplayName: otherUser?.displayName ?? "Unknown",
          status: p.status,
          isIncoming: p.partnerId === user._id,
          createdAt: p.createdAt,
        };
      })
    );

    return result;
  },
});

/**
 * Get a partner's stats (only if accepted partnership exists).
 */
export const getPartnerStats = query({
  args: { partnerId: v.id("users") },
  handler: async (ctx, { partnerId }) => {
    const identity = await ctx.auth.getUserIdentity();
    if (!identity) return null;

    const user = await ctx.db
      .query("users")
      .withIndex("by_externalId", (q) => q.eq("externalId", identity.subject))
      .unique();
    if (!user) return null;

    // Verify accepted partnership exists
    const forward = await ctx.db
      .query("partnerships")
      .withIndex("by_pair", (q) =>
        q.eq("requesterId", user._id).eq("partnerId", partnerId)
      )
      .unique();

    const reverse = await ctx.db
      .query("partnerships")
      .withIndex("by_pair", (q) =>
        q.eq("requesterId", partnerId).eq("partnerId", user._id)
      )
      .unique();

    const partnership = forward ?? reverse;
    if (!partnership || partnership.status !== "accepted") return null;

    const partner = await ctx.db.get(partnerId);
    if (!partner) return null;

    // Get partner's intentions
    const intentions = await ctx.db
      .query("intentionSnapshots")
      .withIndex("by_user_package", (q) => q.eq("userId", partnerId))
      .collect();

    // Get partner's recent sessions (last 7 days)
    const sevenDaysAgo = Date.now() - 7 * 24 * 60 * 60 * 1000;
    const sessions = await ctx.db
      .query("sessionHistory")
      .withIndex("by_user_localId", (q) => q.eq("userId", partnerId))
      .filter((q) => q.gte(q.field("startedAt"), sevenDaysAgo))
      .collect();

    // Get today's stats
    const today = new Date().toISOString().split("T")[0];
    const todayStats = await ctx.db
      .query("dailyStats")
      .withIndex("by_user_date", (q) =>
        q.eq("userId", partnerId).eq("date", today)
      )
      .unique();

    return {
      displayName: partner.displayName,
      intentions,
      recentSessions: sessions,
      todayStats,
    };
  },
});

import { mutation } from "./_generated/server";
import { v } from "convex/values";

/**
 * Sync daily aggregated stats (idempotent upsert by userId + date).
 */
export const syncDailyStats = mutation({
  args: {
    date: v.string(),
    totalXp: v.number(),
    totalCoins: v.number(),
    maxStreak: v.number(),
    sessionsCompleted: v.number(),
    totalFocusMinutes: v.number(),
  },
  handler: async (ctx, args) => {
    const identity = await ctx.auth.getUserIdentity();
    if (!identity) throw new Error("Not authenticated");

    const user = await ctx.db
      .query("users")
      .withIndex("by_externalId", (q) => q.eq("externalId", identity.subject))
      .unique();
    if (!user) throw new Error("User not found");

    const existing = await ctx.db
      .query("dailyStats")
      .withIndex("by_user_date", (q) =>
        q.eq("userId", user._id).eq("date", args.date)
      )
      .unique();

    if (existing) {
      await ctx.db.patch(existing._id, {
        totalXp: args.totalXp,
        totalCoins: args.totalCoins,
        maxStreak: args.maxStreak,
        sessionsCompleted: args.sessionsCompleted,
        totalFocusMinutes: args.totalFocusMinutes,
      });
    } else {
      await ctx.db.insert("dailyStats", {
        userId: user._id,
        ...args,
      });
    }
  },
});

/**
 * Sync a completed session (idempotent by localSessionId).
 */
export const syncSession = mutation({
  args: {
    localSessionId: v.string(),
    name: v.string(),
    goalDurationMinutes: v.number(),
    state: v.string(),
    earnedXp: v.number(),
    startedAt: v.number(),
    endedAt: v.optional(v.number()),
  },
  handler: async (ctx, args) => {
    const identity = await ctx.auth.getUserIdentity();
    if (!identity) throw new Error("Not authenticated");

    const user = await ctx.db
      .query("users")
      .withIndex("by_externalId", (q) => q.eq("externalId", identity.subject))
      .unique();
    if (!user) throw new Error("User not found");

    // Dedup check
    const existing = await ctx.db
      .query("sessionHistory")
      .withIndex("by_user_localId", (q) =>
        q.eq("userId", user._id).eq("localSessionId", args.localSessionId)
      )
      .unique();

    if (existing) return; // Already synced

    await ctx.db.insert("sessionHistory", {
      userId: user._id,
      localSessionId: args.localSessionId,
      name: args.name,
      goalDurationMinutes: args.goalDurationMinutes,
      state: args.state,
      earnedXp: args.earnedXp,
      startedAt: args.startedAt,
      endedAt: args.endedAt,
      syncedAt: Date.now(),
    });
  },
});

/**
 * Sync intention snapshots (upsert by userId + packageName).
 */
export const syncIntentions = mutation({
  args: {
    intentions: v.array(
      v.object({
        packageName: v.string(),
        appName: v.string(),
        streak: v.number(),
        allowedOpensPerDay: v.number(),
        totalOpensToday: v.number(),
      })
    ),
  },
  handler: async (ctx, { intentions }) => {
    const identity = await ctx.auth.getUserIdentity();
    if (!identity) throw new Error("Not authenticated");

    const user = await ctx.db
      .query("users")
      .withIndex("by_externalId", (q) => q.eq("externalId", identity.subject))
      .unique();
    if (!user) throw new Error("User not found");

    for (const intention of intentions) {
      const existing = await ctx.db
        .query("intentionSnapshots")
        .withIndex("by_user_package", (q) =>
          q.eq("userId", user._id).eq("packageName", intention.packageName)
        )
        .unique();

      if (existing) {
        await ctx.db.patch(existing._id, {
          appName: intention.appName,
          streak: intention.streak,
          allowedOpensPerDay: intention.allowedOpensPerDay,
          totalOpensToday: intention.totalOpensToday,
          syncedAt: Date.now(),
        });
      } else {
        await ctx.db.insert("intentionSnapshots", {
          userId: user._id,
          ...intention,
          syncedAt: Date.now(),
        });
      }
    }
  },
});

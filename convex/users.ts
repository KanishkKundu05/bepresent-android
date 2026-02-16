import { mutation, query } from "./_generated/server";
import { v } from "convex/values";

/**
 * Store (upsert) the current user on login.
 * Creates a friend code on first registration.
 */
export const store = mutation({
  args: {},
  handler: async (ctx) => {
    const identity = await ctx.auth.getUserIdentity();
    if (!identity) throw new Error("Not authenticated");

    const externalId = identity.subject;

    // Check if user already exists
    const existing = await ctx.db
      .query("users")
      .withIndex("by_externalId", (q) => q.eq("externalId", externalId))
      .unique();

    if (existing) {
      // Update name if changed
      if (identity.name && identity.name !== existing.name) {
        await ctx.db.patch(existing._id, { name: identity.name });
      }
      return existing._id;
    }

    // Create new user
    const userId = await ctx.db.insert("users", {
      externalId,
      name: identity.name ?? undefined,
      displayName: identity.name ?? `User${Math.floor(Math.random() * 10000)}`,
      createdAt: Date.now(),
    });

    // Generate unique 6-char friend code
    let code: string;
    let codeExists = true;
    do {
      code = generateFriendCode();
      const found = await ctx.db
        .query("friendCodes")
        .withIndex("by_code", (q) => q.eq("code", code))
        .unique();
      codeExists = found !== null;
    } while (codeExists);

    await ctx.db.insert("friendCodes", { userId, code });

    return userId;
  },
});

/**
 * Get current authenticated user.
 */
export const getMe = query({
  args: {},
  handler: async (ctx) => {
    const identity = await ctx.auth.getUserIdentity();
    if (!identity) return null;

    const user = await ctx.db
      .query("users")
      .withIndex("by_externalId", (q) => q.eq("externalId", identity.subject))
      .unique();

    if (!user) return null;

    const friendCode = await ctx.db
      .query("friendCodes")
      .withIndex("by_userId", (q) => q.eq("userId", user._id))
      .unique();

    return { ...user, friendCode: friendCode?.code ?? null };
  },
});

/**
 * Update display name.
 */
export const updateDisplayName = mutation({
  args: { displayName: v.string() },
  handler: async (ctx, { displayName }) => {
    const identity = await ctx.auth.getUserIdentity();
    if (!identity) throw new Error("Not authenticated");

    const user = await ctx.db
      .query("users")
      .withIndex("by_externalId", (q) => q.eq("externalId", identity.subject))
      .unique();

    if (!user) throw new Error("User not found");

    await ctx.db.patch(user._id, { displayName: displayName.trim() });
  },
});

function generateFriendCode(): string {
  const chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // No I/O/0/1 to avoid confusion
  let code = "";
  for (let i = 0; i < 6; i++) {
    code += chars[Math.floor(Math.random() * chars.length)];
  }
  return code;
}

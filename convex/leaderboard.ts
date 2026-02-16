import { query } from "./_generated/server";

/**
 * Global leaderboard: top 20 users by XP in the last 7 days.
 */
export const getGlobal = query({
  args: {},
  handler: async (ctx) => {
    const now = new Date();
    const sevenDaysAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
    const startDate = sevenDaysAgo.toISOString().split("T")[0];

    // Get all daily stats from the last 7 days
    const recentStats = await ctx.db
      .query("dailyStats")
      .withIndex("by_date", (q) => q.gte("date", startDate))
      .collect();

    // Aggregate by user
    const userAgg: Record<
      string,
      { totalXp: number; maxStreak: number; totalFocusMinutes: number }
    > = {};

    for (const stat of recentStats) {
      const uid = stat.userId;
      if (!userAgg[uid]) {
        userAgg[uid] = { totalXp: 0, maxStreak: 0, totalFocusMinutes: 0 };
      }
      userAgg[uid].totalXp += stat.totalXp;
      userAgg[uid].maxStreak = Math.max(
        userAgg[uid].maxStreak,
        stat.maxStreak
      );
      userAgg[uid].totalFocusMinutes += stat.totalFocusMinutes;
    }

    // Sort by XP descending, take top 20
    const sorted = Object.entries(userAgg)
      .sort(([, a], [, b]) => b.totalXp - a.totalXp)
      .slice(0, 20);

    // Fetch user display names
    const entries = await Promise.all(
      sorted.map(async ([userId, agg], index) => {
        const user = await ctx.db.get(userId as any);
        return {
          rank: index + 1,
          userId,
          displayName: user?.displayName ?? "Unknown",
          totalXp: agg.totalXp,
          maxStreak: agg.maxStreak,
          totalFocusMinutes: agg.totalFocusMinutes,
        };
      })
    );

    return entries;
  },
});

/**
 * Friends leaderboard: only accepted partners, ranked by XP last 7 days.
 */
export const getFriends = query({
  args: {},
  handler: async (ctx) => {
    const identity = await ctx.auth.getUserIdentity();
    if (!identity) return [];

    const user = await ctx.db
      .query("users")
      .withIndex("by_externalId", (q) => q.eq("externalId", identity.subject))
      .unique();
    if (!user) return [];

    // Get all accepted partnerships involving this user
    const asRequester = await ctx.db
      .query("partnerships")
      .withIndex("by_requester", (q) => q.eq("requesterId", user._id))
      .filter((q) => q.eq(q.field("status"), "accepted"))
      .collect();

    const asPartner = await ctx.db
      .query("partnerships")
      .withIndex("by_partner", (q) => q.eq("partnerId", user._id))
      .filter((q) => q.eq(q.field("status"), "accepted"))
      .collect();

    const friendIds = new Set<string>();
    friendIds.add(user._id); // Include self
    for (const p of asRequester) friendIds.add(p.partnerId);
    for (const p of asPartner) friendIds.add(p.requesterId);

    // Get last 7 days stats for all friends
    const now = new Date();
    const sevenDaysAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
    const startDate = sevenDaysAgo.toISOString().split("T")[0];

    const recentStats = await ctx.db
      .query("dailyStats")
      .withIndex("by_date", (q) => q.gte("date", startDate))
      .collect();

    const userAgg: Record<
      string,
      { totalXp: number; maxStreak: number; totalFocusMinutes: number }
    > = {};

    for (const stat of recentStats) {
      if (!friendIds.has(stat.userId)) continue;
      const uid = stat.userId;
      if (!userAgg[uid]) {
        userAgg[uid] = { totalXp: 0, maxStreak: 0, totalFocusMinutes: 0 };
      }
      userAgg[uid].totalXp += stat.totalXp;
      userAgg[uid].maxStreak = Math.max(
        userAgg[uid].maxStreak,
        stat.maxStreak
      );
      userAgg[uid].totalFocusMinutes += stat.totalFocusMinutes;
    }

    const sorted = Object.entries(userAgg)
      .sort(([, a], [, b]) => b.totalXp - a.totalXp);

    const entries = await Promise.all(
      sorted.map(async ([userId, agg], index) => {
        const u = await ctx.db.get(userId as any);
        return {
          rank: index + 1,
          userId,
          displayName: u?.displayName ?? "Unknown",
          isMe: userId === user._id,
          totalXp: agg.totalXp,
          maxStreak: agg.maxStreak,
          totalFocusMinutes: agg.totalFocusMinutes,
        };
      })
    );

    return entries;
  },
});

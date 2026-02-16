package com.bepresent.android.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppIntentionDao_Impl implements AppIntentionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AppIntention> __insertionAdapterOfAppIntention;

  private final EntityDeletionOrUpdateAdapter<AppIntention> __deletionAdapterOfAppIntention;

  private final SharedSQLiteStatement __preparedStmtOfIncrementOpens;

  private final SharedSQLiteStatement __preparedStmtOfSetOpenState;

  public AppIntentionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAppIntention = new EntityInsertionAdapter<AppIntention>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `app_intentions` (`id`,`packageName`,`appName`,`allowedOpensPerDay`,`timePerOpenMinutes`,`totalOpensToday`,`streak`,`lastResetDate`,`currentlyOpen`,`openedAt`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AppIntention entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getPackageName());
        statement.bindString(3, entity.getAppName());
        statement.bindLong(4, entity.getAllowedOpensPerDay());
        statement.bindLong(5, entity.getTimePerOpenMinutes());
        statement.bindLong(6, entity.getTotalOpensToday());
        statement.bindLong(7, entity.getStreak());
        statement.bindString(8, entity.getLastResetDate());
        final int _tmp = entity.getCurrentlyOpen() ? 1 : 0;
        statement.bindLong(9, _tmp);
        if (entity.getOpenedAt() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getOpenedAt());
        }
        statement.bindLong(11, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfAppIntention = new EntityDeletionOrUpdateAdapter<AppIntention>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `app_intentions` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AppIntention entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__preparedStmtOfIncrementOpens = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE app_intentions SET totalOpensToday = totalOpensToday + 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSetOpenState = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE app_intentions SET currentlyOpen = ?, openedAt = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final AppIntention intention, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAppIntention.insert(intention);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final AppIntention intention, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfAppIntention.handle(intention);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object incrementOpens(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfIncrementOpens.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfIncrementOpens.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object setOpenState(final String id, final boolean open, final Long openedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetOpenState.acquire();
        int _argIndex = 1;
        final int _tmp = open ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        if (openedAt == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindLong(_argIndex, openedAt);
        }
        _argIndex = 3;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSetOpenState.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AppIntention>> getAll() {
    final String _sql = "SELECT * FROM app_intentions";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"app_intentions"}, new Callable<List<AppIntention>>() {
      @Override
      @NonNull
      public List<AppIntention> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
          final int _cursorIndexOfAllowedOpensPerDay = CursorUtil.getColumnIndexOrThrow(_cursor, "allowedOpensPerDay");
          final int _cursorIndexOfTimePerOpenMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timePerOpenMinutes");
          final int _cursorIndexOfTotalOpensToday = CursorUtil.getColumnIndexOrThrow(_cursor, "totalOpensToday");
          final int _cursorIndexOfStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "streak");
          final int _cursorIndexOfLastResetDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastResetDate");
          final int _cursorIndexOfCurrentlyOpen = CursorUtil.getColumnIndexOrThrow(_cursor, "currentlyOpen");
          final int _cursorIndexOfOpenedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "openedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<AppIntention> _result = new ArrayList<AppIntention>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AppIntention _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final String _tmpAppName;
            _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            final int _tmpAllowedOpensPerDay;
            _tmpAllowedOpensPerDay = _cursor.getInt(_cursorIndexOfAllowedOpensPerDay);
            final int _tmpTimePerOpenMinutes;
            _tmpTimePerOpenMinutes = _cursor.getInt(_cursorIndexOfTimePerOpenMinutes);
            final int _tmpTotalOpensToday;
            _tmpTotalOpensToday = _cursor.getInt(_cursorIndexOfTotalOpensToday);
            final int _tmpStreak;
            _tmpStreak = _cursor.getInt(_cursorIndexOfStreak);
            final String _tmpLastResetDate;
            _tmpLastResetDate = _cursor.getString(_cursorIndexOfLastResetDate);
            final boolean _tmpCurrentlyOpen;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfCurrentlyOpen);
            _tmpCurrentlyOpen = _tmp != 0;
            final Long _tmpOpenedAt;
            if (_cursor.isNull(_cursorIndexOfOpenedAt)) {
              _tmpOpenedAt = null;
            } else {
              _tmpOpenedAt = _cursor.getLong(_cursorIndexOfOpenedAt);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new AppIntention(_tmpId,_tmpPackageName,_tmpAppName,_tmpAllowedOpensPerDay,_tmpTimePerOpenMinutes,_tmpTotalOpensToday,_tmpStreak,_tmpLastResetDate,_tmpCurrentlyOpen,_tmpOpenedAt,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAllOnce(final Continuation<? super List<AppIntention>> $completion) {
    final String _sql = "SELECT * FROM app_intentions";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AppIntention>>() {
      @Override
      @NonNull
      public List<AppIntention> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
          final int _cursorIndexOfAllowedOpensPerDay = CursorUtil.getColumnIndexOrThrow(_cursor, "allowedOpensPerDay");
          final int _cursorIndexOfTimePerOpenMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timePerOpenMinutes");
          final int _cursorIndexOfTotalOpensToday = CursorUtil.getColumnIndexOrThrow(_cursor, "totalOpensToday");
          final int _cursorIndexOfStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "streak");
          final int _cursorIndexOfLastResetDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastResetDate");
          final int _cursorIndexOfCurrentlyOpen = CursorUtil.getColumnIndexOrThrow(_cursor, "currentlyOpen");
          final int _cursorIndexOfOpenedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "openedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<AppIntention> _result = new ArrayList<AppIntention>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AppIntention _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final String _tmpAppName;
            _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            final int _tmpAllowedOpensPerDay;
            _tmpAllowedOpensPerDay = _cursor.getInt(_cursorIndexOfAllowedOpensPerDay);
            final int _tmpTimePerOpenMinutes;
            _tmpTimePerOpenMinutes = _cursor.getInt(_cursorIndexOfTimePerOpenMinutes);
            final int _tmpTotalOpensToday;
            _tmpTotalOpensToday = _cursor.getInt(_cursorIndexOfTotalOpensToday);
            final int _tmpStreak;
            _tmpStreak = _cursor.getInt(_cursorIndexOfStreak);
            final String _tmpLastResetDate;
            _tmpLastResetDate = _cursor.getString(_cursorIndexOfLastResetDate);
            final boolean _tmpCurrentlyOpen;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfCurrentlyOpen);
            _tmpCurrentlyOpen = _tmp != 0;
            final Long _tmpOpenedAt;
            if (_cursor.isNull(_cursorIndexOfOpenedAt)) {
              _tmpOpenedAt = null;
            } else {
              _tmpOpenedAt = _cursor.getLong(_cursorIndexOfOpenedAt);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new AppIntention(_tmpId,_tmpPackageName,_tmpAppName,_tmpAllowedOpensPerDay,_tmpTimePerOpenMinutes,_tmpTotalOpensToday,_tmpStreak,_tmpLastResetDate,_tmpCurrentlyOpen,_tmpOpenedAt,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getByPackage(final String packageName,
      final Continuation<? super AppIntention> $completion) {
    final String _sql = "SELECT * FROM app_intentions WHERE packageName = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, packageName);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AppIntention>() {
      @Override
      @Nullable
      public AppIntention call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
          final int _cursorIndexOfAllowedOpensPerDay = CursorUtil.getColumnIndexOrThrow(_cursor, "allowedOpensPerDay");
          final int _cursorIndexOfTimePerOpenMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timePerOpenMinutes");
          final int _cursorIndexOfTotalOpensToday = CursorUtil.getColumnIndexOrThrow(_cursor, "totalOpensToday");
          final int _cursorIndexOfStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "streak");
          final int _cursorIndexOfLastResetDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastResetDate");
          final int _cursorIndexOfCurrentlyOpen = CursorUtil.getColumnIndexOrThrow(_cursor, "currentlyOpen");
          final int _cursorIndexOfOpenedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "openedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final AppIntention _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final String _tmpAppName;
            _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            final int _tmpAllowedOpensPerDay;
            _tmpAllowedOpensPerDay = _cursor.getInt(_cursorIndexOfAllowedOpensPerDay);
            final int _tmpTimePerOpenMinutes;
            _tmpTimePerOpenMinutes = _cursor.getInt(_cursorIndexOfTimePerOpenMinutes);
            final int _tmpTotalOpensToday;
            _tmpTotalOpensToday = _cursor.getInt(_cursorIndexOfTotalOpensToday);
            final int _tmpStreak;
            _tmpStreak = _cursor.getInt(_cursorIndexOfStreak);
            final String _tmpLastResetDate;
            _tmpLastResetDate = _cursor.getString(_cursorIndexOfLastResetDate);
            final boolean _tmpCurrentlyOpen;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfCurrentlyOpen);
            _tmpCurrentlyOpen = _tmp != 0;
            final Long _tmpOpenedAt;
            if (_cursor.isNull(_cursorIndexOfOpenedAt)) {
              _tmpOpenedAt = null;
            } else {
              _tmpOpenedAt = _cursor.getLong(_cursorIndexOfOpenedAt);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new AppIntention(_tmpId,_tmpPackageName,_tmpAppName,_tmpAllowedOpensPerDay,_tmpTimePerOpenMinutes,_tmpTotalOpensToday,_tmpStreak,_tmpLastResetDate,_tmpCurrentlyOpen,_tmpOpenedAt,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getById(final String id, final Continuation<? super AppIntention> $completion) {
    final String _sql = "SELECT * FROM app_intentions WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AppIntention>() {
      @Override
      @Nullable
      public AppIntention call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
          final int _cursorIndexOfAllowedOpensPerDay = CursorUtil.getColumnIndexOrThrow(_cursor, "allowedOpensPerDay");
          final int _cursorIndexOfTimePerOpenMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timePerOpenMinutes");
          final int _cursorIndexOfTotalOpensToday = CursorUtil.getColumnIndexOrThrow(_cursor, "totalOpensToday");
          final int _cursorIndexOfStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "streak");
          final int _cursorIndexOfLastResetDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastResetDate");
          final int _cursorIndexOfCurrentlyOpen = CursorUtil.getColumnIndexOrThrow(_cursor, "currentlyOpen");
          final int _cursorIndexOfOpenedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "openedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final AppIntention _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final String _tmpAppName;
            _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            final int _tmpAllowedOpensPerDay;
            _tmpAllowedOpensPerDay = _cursor.getInt(_cursorIndexOfAllowedOpensPerDay);
            final int _tmpTimePerOpenMinutes;
            _tmpTimePerOpenMinutes = _cursor.getInt(_cursorIndexOfTimePerOpenMinutes);
            final int _tmpTotalOpensToday;
            _tmpTotalOpensToday = _cursor.getInt(_cursorIndexOfTotalOpensToday);
            final int _tmpStreak;
            _tmpStreak = _cursor.getInt(_cursorIndexOfStreak);
            final String _tmpLastResetDate;
            _tmpLastResetDate = _cursor.getString(_cursorIndexOfLastResetDate);
            final boolean _tmpCurrentlyOpen;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfCurrentlyOpen);
            _tmpCurrentlyOpen = _tmp != 0;
            final Long _tmpOpenedAt;
            if (_cursor.isNull(_cursorIndexOfOpenedAt)) {
              _tmpOpenedAt = null;
            } else {
              _tmpOpenedAt = _cursor.getLong(_cursorIndexOfOpenedAt);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new AppIntention(_tmpId,_tmpPackageName,_tmpAppName,_tmpAllowedOpensPerDay,_tmpTimePerOpenMinutes,_tmpTotalOpensToday,_tmpStreak,_tmpLastResetDate,_tmpCurrentlyOpen,_tmpOpenedAt,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getBlockedIntentions(final Continuation<? super List<AppIntention>> $completion) {
    final String _sql = "SELECT * FROM app_intentions WHERE currentlyOpen = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AppIntention>>() {
      @Override
      @NonNull
      public List<AppIntention> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
          final int _cursorIndexOfAllowedOpensPerDay = CursorUtil.getColumnIndexOrThrow(_cursor, "allowedOpensPerDay");
          final int _cursorIndexOfTimePerOpenMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timePerOpenMinutes");
          final int _cursorIndexOfTotalOpensToday = CursorUtil.getColumnIndexOrThrow(_cursor, "totalOpensToday");
          final int _cursorIndexOfStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "streak");
          final int _cursorIndexOfLastResetDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastResetDate");
          final int _cursorIndexOfCurrentlyOpen = CursorUtil.getColumnIndexOrThrow(_cursor, "currentlyOpen");
          final int _cursorIndexOfOpenedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "openedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<AppIntention> _result = new ArrayList<AppIntention>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AppIntention _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final String _tmpAppName;
            _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            final int _tmpAllowedOpensPerDay;
            _tmpAllowedOpensPerDay = _cursor.getInt(_cursorIndexOfAllowedOpensPerDay);
            final int _tmpTimePerOpenMinutes;
            _tmpTimePerOpenMinutes = _cursor.getInt(_cursorIndexOfTimePerOpenMinutes);
            final int _tmpTotalOpensToday;
            _tmpTotalOpensToday = _cursor.getInt(_cursorIndexOfTotalOpensToday);
            final int _tmpStreak;
            _tmpStreak = _cursor.getInt(_cursorIndexOfStreak);
            final String _tmpLastResetDate;
            _tmpLastResetDate = _cursor.getString(_cursorIndexOfLastResetDate);
            final boolean _tmpCurrentlyOpen;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfCurrentlyOpen);
            _tmpCurrentlyOpen = _tmp != 0;
            final Long _tmpOpenedAt;
            if (_cursor.isNull(_cursorIndexOfOpenedAt)) {
              _tmpOpenedAt = null;
            } else {
              _tmpOpenedAt = _cursor.getLong(_cursorIndexOfOpenedAt);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new AppIntention(_tmpId,_tmpPackageName,_tmpAppName,_tmpAllowedOpensPerDay,_tmpTimePerOpenMinutes,_tmpTotalOpensToday,_tmpStreak,_tmpLastResetDate,_tmpCurrentlyOpen,_tmpOpenedAt,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM app_intentions";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

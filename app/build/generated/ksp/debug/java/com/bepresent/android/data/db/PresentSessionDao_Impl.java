package com.bepresent.android.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
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
public final class PresentSessionDao_Impl implements PresentSessionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PresentSession> __insertionAdapterOfPresentSession;

  private final EntityInsertionAdapter<PresentSessionAction> __insertionAdapterOfPresentSessionAction;

  public PresentSessionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPresentSession = new EntityInsertionAdapter<PresentSession>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `present_sessions` (`id`,`name`,`goalDurationMinutes`,`beastMode`,`state`,`blockedPackages`,`startedAt`,`goalReachedAt`,`endedAt`,`earnedXp`,`earnedCoins`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PresentSession entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getGoalDurationMinutes());
        final int _tmp = entity.getBeastMode() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindString(5, entity.getState());
        statement.bindString(6, entity.getBlockedPackages());
        if (entity.getStartedAt() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getStartedAt());
        }
        if (entity.getGoalReachedAt() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getGoalReachedAt());
        }
        if (entity.getEndedAt() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getEndedAt());
        }
        statement.bindLong(10, entity.getEarnedXp());
        statement.bindLong(11, entity.getEarnedCoins());
        statement.bindLong(12, entity.getCreatedAt());
      }
    };
    this.__insertionAdapterOfPresentSessionAction = new EntityInsertionAdapter<PresentSessionAction>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `present_session_actions` (`id`,`sessionId`,`action`,`timestamp`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PresentSessionAction entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        statement.bindString(3, entity.getAction());
        statement.bindLong(4, entity.getTimestamp());
      }
    };
  }

  @Override
  public Object upsert(final PresentSession session, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPresentSession.insert(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAction(final PresentSessionAction action,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPresentSessionAction.insert(action);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getActiveSession(final Continuation<? super PresentSession> $completion) {
    final String _sql = "SELECT * FROM present_sessions WHERE state IN ('active', 'goalReached') LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PresentSession>() {
      @Override
      @Nullable
      public PresentSession call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfGoalDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "goalDurationMinutes");
          final int _cursorIndexOfBeastMode = CursorUtil.getColumnIndexOrThrow(_cursor, "beastMode");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfBlockedPackages = CursorUtil.getColumnIndexOrThrow(_cursor, "blockedPackages");
          final int _cursorIndexOfStartedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "startedAt");
          final int _cursorIndexOfGoalReachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "goalReachedAt");
          final int _cursorIndexOfEndedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "endedAt");
          final int _cursorIndexOfEarnedXp = CursorUtil.getColumnIndexOrThrow(_cursor, "earnedXp");
          final int _cursorIndexOfEarnedCoins = CursorUtil.getColumnIndexOrThrow(_cursor, "earnedCoins");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final PresentSession _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpGoalDurationMinutes;
            _tmpGoalDurationMinutes = _cursor.getInt(_cursorIndexOfGoalDurationMinutes);
            final boolean _tmpBeastMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfBeastMode);
            _tmpBeastMode = _tmp != 0;
            final String _tmpState;
            _tmpState = _cursor.getString(_cursorIndexOfState);
            final String _tmpBlockedPackages;
            _tmpBlockedPackages = _cursor.getString(_cursorIndexOfBlockedPackages);
            final Long _tmpStartedAt;
            if (_cursor.isNull(_cursorIndexOfStartedAt)) {
              _tmpStartedAt = null;
            } else {
              _tmpStartedAt = _cursor.getLong(_cursorIndexOfStartedAt);
            }
            final Long _tmpGoalReachedAt;
            if (_cursor.isNull(_cursorIndexOfGoalReachedAt)) {
              _tmpGoalReachedAt = null;
            } else {
              _tmpGoalReachedAt = _cursor.getLong(_cursorIndexOfGoalReachedAt);
            }
            final Long _tmpEndedAt;
            if (_cursor.isNull(_cursorIndexOfEndedAt)) {
              _tmpEndedAt = null;
            } else {
              _tmpEndedAt = _cursor.getLong(_cursorIndexOfEndedAt);
            }
            final int _tmpEarnedXp;
            _tmpEarnedXp = _cursor.getInt(_cursorIndexOfEarnedXp);
            final int _tmpEarnedCoins;
            _tmpEarnedCoins = _cursor.getInt(_cursorIndexOfEarnedCoins);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new PresentSession(_tmpId,_tmpName,_tmpGoalDurationMinutes,_tmpBeastMode,_tmpState,_tmpBlockedPackages,_tmpStartedAt,_tmpGoalReachedAt,_tmpEndedAt,_tmpEarnedXp,_tmpEarnedCoins,_tmpCreatedAt);
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
  public Flow<PresentSession> observeActiveSession() {
    final String _sql = "SELECT * FROM present_sessions WHERE state IN ('active', 'goalReached') LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"present_sessions"}, new Callable<PresentSession>() {
      @Override
      @Nullable
      public PresentSession call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfGoalDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "goalDurationMinutes");
          final int _cursorIndexOfBeastMode = CursorUtil.getColumnIndexOrThrow(_cursor, "beastMode");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfBlockedPackages = CursorUtil.getColumnIndexOrThrow(_cursor, "blockedPackages");
          final int _cursorIndexOfStartedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "startedAt");
          final int _cursorIndexOfGoalReachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "goalReachedAt");
          final int _cursorIndexOfEndedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "endedAt");
          final int _cursorIndexOfEarnedXp = CursorUtil.getColumnIndexOrThrow(_cursor, "earnedXp");
          final int _cursorIndexOfEarnedCoins = CursorUtil.getColumnIndexOrThrow(_cursor, "earnedCoins");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final PresentSession _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpGoalDurationMinutes;
            _tmpGoalDurationMinutes = _cursor.getInt(_cursorIndexOfGoalDurationMinutes);
            final boolean _tmpBeastMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfBeastMode);
            _tmpBeastMode = _tmp != 0;
            final String _tmpState;
            _tmpState = _cursor.getString(_cursorIndexOfState);
            final String _tmpBlockedPackages;
            _tmpBlockedPackages = _cursor.getString(_cursorIndexOfBlockedPackages);
            final Long _tmpStartedAt;
            if (_cursor.isNull(_cursorIndexOfStartedAt)) {
              _tmpStartedAt = null;
            } else {
              _tmpStartedAt = _cursor.getLong(_cursorIndexOfStartedAt);
            }
            final Long _tmpGoalReachedAt;
            if (_cursor.isNull(_cursorIndexOfGoalReachedAt)) {
              _tmpGoalReachedAt = null;
            } else {
              _tmpGoalReachedAt = _cursor.getLong(_cursorIndexOfGoalReachedAt);
            }
            final Long _tmpEndedAt;
            if (_cursor.isNull(_cursorIndexOfEndedAt)) {
              _tmpEndedAt = null;
            } else {
              _tmpEndedAt = _cursor.getLong(_cursorIndexOfEndedAt);
            }
            final int _tmpEarnedXp;
            _tmpEarnedXp = _cursor.getInt(_cursorIndexOfEarnedXp);
            final int _tmpEarnedCoins;
            _tmpEarnedCoins = _cursor.getInt(_cursorIndexOfEarnedCoins);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new PresentSession(_tmpId,_tmpName,_tmpGoalDurationMinutes,_tmpBeastMode,_tmpState,_tmpBlockedPackages,_tmpStartedAt,_tmpGoalReachedAt,_tmpEndedAt,_tmpEarnedXp,_tmpEarnedCoins,_tmpCreatedAt);
          } else {
            _result = null;
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
  public Flow<List<PresentSession>> getAllSessions() {
    final String _sql = "SELECT * FROM present_sessions ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"present_sessions"}, new Callable<List<PresentSession>>() {
      @Override
      @NonNull
      public List<PresentSession> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfGoalDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "goalDurationMinutes");
          final int _cursorIndexOfBeastMode = CursorUtil.getColumnIndexOrThrow(_cursor, "beastMode");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfBlockedPackages = CursorUtil.getColumnIndexOrThrow(_cursor, "blockedPackages");
          final int _cursorIndexOfStartedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "startedAt");
          final int _cursorIndexOfGoalReachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "goalReachedAt");
          final int _cursorIndexOfEndedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "endedAt");
          final int _cursorIndexOfEarnedXp = CursorUtil.getColumnIndexOrThrow(_cursor, "earnedXp");
          final int _cursorIndexOfEarnedCoins = CursorUtil.getColumnIndexOrThrow(_cursor, "earnedCoins");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<PresentSession> _result = new ArrayList<PresentSession>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PresentSession _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpGoalDurationMinutes;
            _tmpGoalDurationMinutes = _cursor.getInt(_cursorIndexOfGoalDurationMinutes);
            final boolean _tmpBeastMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfBeastMode);
            _tmpBeastMode = _tmp != 0;
            final String _tmpState;
            _tmpState = _cursor.getString(_cursorIndexOfState);
            final String _tmpBlockedPackages;
            _tmpBlockedPackages = _cursor.getString(_cursorIndexOfBlockedPackages);
            final Long _tmpStartedAt;
            if (_cursor.isNull(_cursorIndexOfStartedAt)) {
              _tmpStartedAt = null;
            } else {
              _tmpStartedAt = _cursor.getLong(_cursorIndexOfStartedAt);
            }
            final Long _tmpGoalReachedAt;
            if (_cursor.isNull(_cursorIndexOfGoalReachedAt)) {
              _tmpGoalReachedAt = null;
            } else {
              _tmpGoalReachedAt = _cursor.getLong(_cursorIndexOfGoalReachedAt);
            }
            final Long _tmpEndedAt;
            if (_cursor.isNull(_cursorIndexOfEndedAt)) {
              _tmpEndedAt = null;
            } else {
              _tmpEndedAt = _cursor.getLong(_cursorIndexOfEndedAt);
            }
            final int _tmpEarnedXp;
            _tmpEarnedXp = _cursor.getInt(_cursorIndexOfEarnedXp);
            final int _tmpEarnedCoins;
            _tmpEarnedCoins = _cursor.getInt(_cursorIndexOfEarnedCoins);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new PresentSession(_tmpId,_tmpName,_tmpGoalDurationMinutes,_tmpBeastMode,_tmpState,_tmpBlockedPackages,_tmpStartedAt,_tmpGoalReachedAt,_tmpEndedAt,_tmpEarnedXp,_tmpEarnedCoins,_tmpCreatedAt);
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
  public Object getById(final String id, final Continuation<? super PresentSession> $completion) {
    final String _sql = "SELECT * FROM present_sessions WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PresentSession>() {
      @Override
      @Nullable
      public PresentSession call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfGoalDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "goalDurationMinutes");
          final int _cursorIndexOfBeastMode = CursorUtil.getColumnIndexOrThrow(_cursor, "beastMode");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfBlockedPackages = CursorUtil.getColumnIndexOrThrow(_cursor, "blockedPackages");
          final int _cursorIndexOfStartedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "startedAt");
          final int _cursorIndexOfGoalReachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "goalReachedAt");
          final int _cursorIndexOfEndedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "endedAt");
          final int _cursorIndexOfEarnedXp = CursorUtil.getColumnIndexOrThrow(_cursor, "earnedXp");
          final int _cursorIndexOfEarnedCoins = CursorUtil.getColumnIndexOrThrow(_cursor, "earnedCoins");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final PresentSession _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpGoalDurationMinutes;
            _tmpGoalDurationMinutes = _cursor.getInt(_cursorIndexOfGoalDurationMinutes);
            final boolean _tmpBeastMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfBeastMode);
            _tmpBeastMode = _tmp != 0;
            final String _tmpState;
            _tmpState = _cursor.getString(_cursorIndexOfState);
            final String _tmpBlockedPackages;
            _tmpBlockedPackages = _cursor.getString(_cursorIndexOfBlockedPackages);
            final Long _tmpStartedAt;
            if (_cursor.isNull(_cursorIndexOfStartedAt)) {
              _tmpStartedAt = null;
            } else {
              _tmpStartedAt = _cursor.getLong(_cursorIndexOfStartedAt);
            }
            final Long _tmpGoalReachedAt;
            if (_cursor.isNull(_cursorIndexOfGoalReachedAt)) {
              _tmpGoalReachedAt = null;
            } else {
              _tmpGoalReachedAt = _cursor.getLong(_cursorIndexOfGoalReachedAt);
            }
            final Long _tmpEndedAt;
            if (_cursor.isNull(_cursorIndexOfEndedAt)) {
              _tmpEndedAt = null;
            } else {
              _tmpEndedAt = _cursor.getLong(_cursorIndexOfEndedAt);
            }
            final int _tmpEarnedXp;
            _tmpEarnedXp = _cursor.getInt(_cursorIndexOfEarnedXp);
            final int _tmpEarnedCoins;
            _tmpEarnedCoins = _cursor.getInt(_cursorIndexOfEarnedCoins);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new PresentSession(_tmpId,_tmpName,_tmpGoalDurationMinutes,_tmpBeastMode,_tmpState,_tmpBlockedPackages,_tmpStartedAt,_tmpGoalReachedAt,_tmpEndedAt,_tmpEarnedXp,_tmpEarnedCoins,_tmpCreatedAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

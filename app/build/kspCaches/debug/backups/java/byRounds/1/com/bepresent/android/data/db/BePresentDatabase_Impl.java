package com.bepresent.android.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class BePresentDatabase_Impl extends BePresentDatabase {
  private volatile AppIntentionDao _appIntentionDao;

  private volatile PresentSessionDao _presentSessionDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `app_intentions` (`id` TEXT NOT NULL, `packageName` TEXT NOT NULL, `appName` TEXT NOT NULL, `allowedOpensPerDay` INTEGER NOT NULL, `timePerOpenMinutes` INTEGER NOT NULL, `totalOpensToday` INTEGER NOT NULL, `streak` INTEGER NOT NULL, `lastResetDate` TEXT NOT NULL, `currentlyOpen` INTEGER NOT NULL, `openedAt` INTEGER, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `present_sessions` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `goalDurationMinutes` INTEGER NOT NULL, `beastMode` INTEGER NOT NULL, `state` TEXT NOT NULL, `blockedPackages` TEXT NOT NULL, `startedAt` INTEGER, `goalReachedAt` INTEGER, `endedAt` INTEGER, `earnedXp` INTEGER NOT NULL, `earnedCoins` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `present_session_actions` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `action` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '3c39cc5fc0c4e813377762e28e8db662')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `app_intentions`");
        db.execSQL("DROP TABLE IF EXISTS `present_sessions`");
        db.execSQL("DROP TABLE IF EXISTS `present_session_actions`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsAppIntentions = new HashMap<String, TableInfo.Column>(11);
        _columnsAppIntentions.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppIntentions.put("packageName", new TableInfo.Column("packageName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppIntentions.put("appName", new TableInfo.Column("appName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppIntentions.put("allowedOpensPerDay", new TableInfo.Column("allowedOpensPerDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppIntentions.put("timePerOpenMinutes", new TableInfo.Column("timePerOpenMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppIntentions.put("totalOpensToday", new TableInfo.Column("totalOpensToday", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppIntentions.put("streak", new TableInfo.Column("streak", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppIntentions.put("lastResetDate", new TableInfo.Column("lastResetDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppIntentions.put("currentlyOpen", new TableInfo.Column("currentlyOpen", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppIntentions.put("openedAt", new TableInfo.Column("openedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppIntentions.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAppIntentions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAppIntentions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAppIntentions = new TableInfo("app_intentions", _columnsAppIntentions, _foreignKeysAppIntentions, _indicesAppIntentions);
        final TableInfo _existingAppIntentions = TableInfo.read(db, "app_intentions");
        if (!_infoAppIntentions.equals(_existingAppIntentions)) {
          return new RoomOpenHelper.ValidationResult(false, "app_intentions(com.bepresent.android.data.db.AppIntention).\n"
                  + " Expected:\n" + _infoAppIntentions + "\n"
                  + " Found:\n" + _existingAppIntentions);
        }
        final HashMap<String, TableInfo.Column> _columnsPresentSessions = new HashMap<String, TableInfo.Column>(12);
        _columnsPresentSessions.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPresentSessions.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPresentSessions.put("goalDurationMinutes", new TableInfo.Column("goalDurationMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPresentSessions.put("beastMode", new TableInfo.Column("beastMode", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPresentSessions.put("state", new TableInfo.Column("state", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPresentSessions.put("blockedPackages", new TableInfo.Column("blockedPackages", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPresentSessions.put("startedAt", new TableInfo.Column("startedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPresentSessions.put("goalReachedAt", new TableInfo.Column("goalReachedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPresentSessions.put("endedAt", new TableInfo.Column("endedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPresentSessions.put("earnedXp", new TableInfo.Column("earnedXp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPresentSessions.put("earnedCoins", new TableInfo.Column("earnedCoins", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPresentSessions.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPresentSessions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPresentSessions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPresentSessions = new TableInfo("present_sessions", _columnsPresentSessions, _foreignKeysPresentSessions, _indicesPresentSessions);
        final TableInfo _existingPresentSessions = TableInfo.read(db, "present_sessions");
        if (!_infoPresentSessions.equals(_existingPresentSessions)) {
          return new RoomOpenHelper.ValidationResult(false, "present_sessions(com.bepresent.android.data.db.PresentSession).\n"
                  + " Expected:\n" + _infoPresentSessions + "\n"
                  + " Found:\n" + _existingPresentSessions);
        }
        final HashMap<String, TableInfo.Column> _columnsPresentSessionActions = new HashMap<String, TableInfo.Column>(4);
        _columnsPresentSessionActions.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPresentSessionActions.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPresentSessionActions.put("action", new TableInfo.Column("action", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPresentSessionActions.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPresentSessionActions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPresentSessionActions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPresentSessionActions = new TableInfo("present_session_actions", _columnsPresentSessionActions, _foreignKeysPresentSessionActions, _indicesPresentSessionActions);
        final TableInfo _existingPresentSessionActions = TableInfo.read(db, "present_session_actions");
        if (!_infoPresentSessionActions.equals(_existingPresentSessionActions)) {
          return new RoomOpenHelper.ValidationResult(false, "present_session_actions(com.bepresent.android.data.db.PresentSessionAction).\n"
                  + " Expected:\n" + _infoPresentSessionActions + "\n"
                  + " Found:\n" + _existingPresentSessionActions);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "3c39cc5fc0c4e813377762e28e8db662", "69e70317dd2a10ec01e4b5cbd3670e68");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "app_intentions","present_sessions","present_session_actions");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `app_intentions`");
      _db.execSQL("DELETE FROM `present_sessions`");
      _db.execSQL("DELETE FROM `present_session_actions`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(AppIntentionDao.class, AppIntentionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PresentSessionDao.class, PresentSessionDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public AppIntentionDao appIntentionDao() {
    if (_appIntentionDao != null) {
      return _appIntentionDao;
    } else {
      synchronized(this) {
        if(_appIntentionDao == null) {
          _appIntentionDao = new AppIntentionDao_Impl(this);
        }
        return _appIntentionDao;
      }
    }
  }

  @Override
  public PresentSessionDao presentSessionDao() {
    if (_presentSessionDao != null) {
      return _presentSessionDao;
    } else {
      synchronized(this) {
        if(_presentSessionDao == null) {
          _presentSessionDao = new PresentSessionDao_Impl(this);
        }
        return _presentSessionDao;
      }
    }
  }
}

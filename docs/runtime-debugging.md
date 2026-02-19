# Android Runtime Debugging (Blocking Flow)

## 1) Start live logs from your Mac

```bash
adb devices
adb logcat -c
adb logcat -v time \
  BP_Monitor:D BP_BlockAct:D BP_Shield:D BP_Usage:D \
  BP_Intention:D BP_IntAlarm:D BP_IntAlarmSched:D \
  BP_Session:D BP_SessionAlarm:D BP_SessionAlarmSched:D \
  BP_Dev:D *:S
```

If you also want Android system messages about blocked activity launches:

```bash
adb logcat -v time ActivityTaskManager:I WindowManager:I BP_Monitor:D BP_BlockAct:D BP_Shield:D *:S
```

## 2) Use in-app Dev Tools

Open `Dashboard -> Dev Tools` and use:

- `Start` under Monitoring Service
- `Test Session Shield` and `Test Intention Shield`
- `Runtime Logs` card (live in-app log buffer)

## 3) Blocking pipeline checkpoints

You should see this order in logs:

1. `BP_Monitor` detects a foreground package and marks it blocked
2. `BP_Monitor launchBlockedActivity` attempts shield launch
3. `BP_BlockAct onCreate/onResume` confirms shield activity became visible
4. `BP_Shield` confirms selected shield variant rendered

If step 2 appears but step 3 never appears, Android is likely blocking background activity launch.  
In that case, a notification `Shield launch needs user tap` is posted; tap it to open shield manually.

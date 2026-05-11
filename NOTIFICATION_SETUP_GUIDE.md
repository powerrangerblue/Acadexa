# Push Notification Setup Guide for ACADEXA

## Problem Summary
Push notifications weren't working on real mobile phones. This has been fixed with the following improvements:

## Changes Made

### 1. **Runtime Permission Request** ✓
- Added automatic request for POST_NOTIFICATIONS permission on Android 13+
- The app will now prompt users when they first open the dashboard
- Triggers immediate notification check after permission is granted

### 2. **Improved Schedule Notification Logic** ✓
- Fixed bug where schedules only worked if matching "today's" day name
- Added `isScheduledSoon()` method that checks both today and tomorrow's schedules
- Now properly calculates if a class is starting within 30 minutes

### 3. **Enhanced Notification Worker** ✓
- Increased check frequency from 15 minutes to 5 minutes minimum
- Added immediate notification check when app is opened
- Better error handling and resilience

### 4. **Notification Channel Setup** ✓
- Ensures notification channel is created with HIGH importance
- Automatically called when dashboard loads

---

## Testing Notifications on Real Phone

### Step 1: Grant Permissions
1. Open ACADEXA on your phone
2. A permission dialog will appear asking to allow notifications
3. **Tap "Allow"** to grant POST_NOTIFICATIONS permission
4. Check your notifications immediately (you should see a test notification if you just granted permission)

### Step 2: Verify Background Restrictions Are OFF
**Critical for real devices**: Disable battery optimization for ACADEXA

**For Samsung/OneUI:**
1. Settings > Battery > Battery Saver
2. Swipe left to "Apps" tab
3. Find "ACADEXA" and tap it
4. Select "Don't optimize" or "Not optimized"

**For other Android devices:**
1. Settings > Battery > Battery Optimization (or similar)
2. Find ACADEXA and remove it from battery optimization

**For Google Play Services:**
1. Settings > Apps > Permissions > Permissions manager
2. Notifications - ensure ACADEXA is allowed

### Step 3: Test with a Task
1. Tap "Tasks" tab
2. Add a new task with due date/time set to **5 minutes from now**
3. Wait a few seconds
4. You should see a push notification appear

### Step 4: Test with a Schedule
1. Tap "Calendar" or go to add schedule
2. Add a schedule for **today** (not tomorrow)
3. Set the time to **5 minutes from now**
4. Wait a few seconds
5. You should receive a notification

---

## Troubleshooting

### Notifications Not Appearing?

**1. Check Permission Status**
```
adb shell pm dump com.example.acadexa | grep "android.permission.POST_NOTIFICATIONS"
```
Should show: `granted`

**2. Check Battery Optimization**
- Go to Settings > Apps > ACADEXA > Battery
- Ensure "Allow unrestricted battery usage" is enabled
- Or disable battery optimization specifically for this app

**3. Check Do Not Disturb**
- Make sure your phone isn't in Do Not Disturb mode
- Check notification settings for the app

**4. Check Notification Channel**
- Settings > Apps > ACADEXA > Notifications
- Ensure "Acadexa Alerts" channel is enabled
- Importance should be set to "Urgent" or "High"

**5. Check Time Accuracy**
- Verify task/schedule times are in the future
- Must be within 30 minutes of current time to trigger notification
- Phone time must be correct

**6. Force Notification Check**
- Open app dashboard and grant (or re-grant) notification permission
- This triggers an immediate check

### Still Not Working?

**On Real Device - Enable Developer Settings:**
```
adb logcat | grep "NotificationChecker"
adb logcat | grep "acadexa_notification_checks"
```

This will show if the worker is running.

**Check Work Manager Logs:**
```
adb shell dumpsys jobscheduler | grep acadexa
```

---

## What Happens Behind the Scenes

1. **App Launch**
   - Requests POST_NOTIFICATIONS permission (if needed)
   - Ensures notification channel exists
   - Schedules periodic background checks

2. **Background Checks** (Every 5-15 minutes)
   - Worker checks all user's tasks
   - Worker checks all user's schedules
   - Compares times with current time
   - If within 30 minutes: creates database entry + sends notification

3. **Notification Delivery**
   - Android handles the actual notification display
   - Uses high-priority "Acadexa Alerts" channel
   - Notification opens dashboard when tapped

---

## Important Notes

- **Minimum 30 minutes before**: Notifications only trigger if event is within 30 minutes
- **Exact match not required**: "Monday" schedule works on any Monday, not just the first one
- **Background required**: App must have permission to run background tasks
- **Connection not required**: Notifications use local database, no server needed

---

## If Issues Persist

1. **Uninstall and Reinstall** the app
2. **Grant all permissions** when prompted
3. **Disable battery optimization** for ACADEXA
4. **Clear app data** (Settings > Apps > ACADEXA > Storage > Clear Cache/Data)
5. **Restart phone**
6. **Try adding task/schedule** again with time 5 minutes from now

---

## Technical Details

**Notification Settings Used:**
- Channel ID: `acadexa_alerts`
- Channel Name: `Acadexa Alerts`  
- Importance Level: HIGH (causes notification to appear on top)
- Auto-cancel: Yes (notification dismisses when tapped)
- Priority: HIGH

**Deduplication Window:**
- Same notification won't appear twice within 45 minutes
- Prevents spam for recurring schedules

**Check Timing:**
- Periodic: Every 5-15 minutes via WorkManager
- Immediate: When app opens
- Manual: Can be triggered by permission grant

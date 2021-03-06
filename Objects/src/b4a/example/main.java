package b4a.example;


import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = false;
	public static final boolean includeTitle = false;
    public static WeakReference<Activity> previousOne;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFirst) {
			processBA = new anywheresoftware.b4a.ShellBA(this.getApplicationContext(), null, null, "b4a.example", "b4a.example.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (main).");
				p.finish();
			}
		}
        processBA.runHook("oncreate", this, null);
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		mostCurrent = this;
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
        WaitForLayout wl = new WaitForLayout();
        if (anywheresoftware.b4a.objects.ServiceHelper.StarterHelper.startFromActivity(processBA, wl, false))
		    BA.handler.postDelayed(wl, 5);

	}
	static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "b4a.example", "b4a.example.main");
        
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "b4a.example.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density, mostCurrent);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (main) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
        try {
            if (processBA.subExists("activity_actionbarhomeclick")) {
                Class.forName("android.app.ActionBar").getMethod("setHomeButtonEnabled", boolean.class).invoke(
                    getClass().getMethod("getActionBar").invoke(this), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (processBA.runHook("oncreateoptionsmenu", this, new Object[] {menu}))
            return true;
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
        
		return true;
	}   
 @Override
 public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == 16908332) {
        processBA.raiseEvent(null, "activity_actionbarhomeclick");
        return true;
    }
    else
        return super.onOptionsItemSelected(item); 
}
@Override
 public boolean onPrepareOptionsMenu(android.view.Menu menu) {
    super.onPrepareOptionsMenu(menu);
    processBA.runHook("onprepareoptionsmenu", this, new Object[] {menu});
    return true;
    
 }
 protected void onStart() {
    super.onStart();
    processBA.runHook("onstart", this, null);
}
 protected void onStop() {
    super.onStop();
    processBA.runHook("onstop", this, null);
}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEvent(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
		this.setIntent(intent);
        processBA.runHook("onnewintent", this, new Object[] {intent});
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null) //workaround for emulator bug (Issue 2423)
            return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        BA.LogInfo("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        processBA.setActivityPaused(true);
        mostCurrent = null;
        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        processBA.runHook("onpause", this, null);
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
        processBA.runHook("ondestroy", this, null);
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
        processBA.runHook("onresume", this, null);
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
			if (mostCurrent == null || mostCurrent != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (main) Resume **");
		    processBA.raiseEvent(mostCurrent._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
        processBA.runHook("onactivityresult", this, new Object[] {requestCode, resultCode});
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}
    public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
        Object[] o;
        if (permissions.length > 0)
            o = new Object[] {permissions[0], grantResults[0] == 0};
        else
            o = new Object[] {"", false};
        processBA.raiseEventFromDifferentThread(null,null, 0, "activity_permissionresult", true, o);
            
    }



public static void initializeProcessGlobals() {
    
    if (main.processGlobalsRun == false) {
	    main.processGlobalsRun = true;
		try {
		        		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}
public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}

private static BA killProgramHelper(BA ba) {
    if (ba == null)
        return null;
    anywheresoftware.b4a.BA.SharedProcessBA sharedProcessBA = ba.sharedProcessBA;
    if (sharedProcessBA == null || sharedProcessBA.activityBA == null)
        return null;
    return sharedProcessBA.activityBA.get();
}
public static void killProgram() {
     {
            Activity __a = null;
            if (main.previousOne != null) {
				__a = main.previousOne.get();
			}
            else {
                BA ba = killProgramHelper(main.mostCurrent == null ? null : main.mostCurrent.processBA);
                if (ba != null) __a = ba.activity;
            }
            if (__a != null)
				__a.finish();}

BA.applicationContext.stopService(new android.content.Intent(BA.applicationContext, starter.class));
}
public anywheresoftware.b4a.keywords.Common __c = null;
public anywheresoftware.b4a.objects.PanelWrapper _panellogin = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _splashlogo = null;
public anywheresoftware.b4a.objects.SpinnerWrapper _usuarios = null;
public anywheresoftware.b4a.objects.PanelWrapper _panelrojo = null;
public anywheresoftware.b4a.objects.LabelWrapper _labelseleccion = null;
public anywheresoftware.b4a.objects.PanelWrapper _panelcarta = null;
public anywheresoftware.b4a.objects.PanelWrapper _panelsombra = null;
public anywheresoftware.b4a.objects.ButtonWrapper _botonacceder = null;
public anywheresoftware.b4a.objects.ButtonWrapper _botoncancelar = null;
public flm.b4a.animationplus.AnimationPlusWrapper _animacionarriba = null;
public anywheresoftware.b4a.objects.AnimationWrapper _animacionfade = null;
public anywheresoftware.b4a.objects.AnimationWrapper _animacionfaderojo = null;
public flm.b4a.animationplus.AnimationSet _animaciones = null;
public anywheresoftware.b4a.agraham.dialogs.InputDialog _input = null;
public static String _password = "";
public b4a.example.starter _starter = null;
public static String  _activity_create(boolean _firsttime) throws Exception{
RDebugUtils.currentModule="main";
if (Debug.shouldDelegate(mostCurrent.activityBA, "activity_create"))
	return (String) Debug.delegate(mostCurrent.activityBA, "activity_create", new Object[] {_firsttime});
RDebugUtils.currentLine=131072;
 //BA.debugLineNum = 131072;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
RDebugUtils.currentLine=131073;
 //BA.debugLineNum = 131073;BA.debugLine="Activity.LoadLayout(\"Splash\")'";
mostCurrent._activity.LoadLayout("Splash",mostCurrent.activityBA);
RDebugUtils.currentLine=131075;
 //BA.debugLineNum = 131075;BA.debugLine="Usuarios.AddAll(Array As String( \"Gerente\", \"Chef";
mostCurrent._usuarios.AddAll(anywheresoftware.b4a.keywords.Common.ArrayToList(new String[]{"Gerente","Chef","Mesero"}));
RDebugUtils.currentLine=131076;
 //BA.debugLineNum = 131076;BA.debugLine="Usuarios.DropdownBackgroundColor = Colors.RGB(255";
mostCurrent._usuarios.setDropdownBackgroundColor(anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (255),(int) (240),(int) (203)));
RDebugUtils.currentLine=131077;
 //BA.debugLineNum = 131077;BA.debugLine="Usuarios.DropdownTextColor = Colors.RGB(234, 117,";
mostCurrent._usuarios.setDropdownTextColor(anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (234),(int) (117),(int) (108)));
RDebugUtils.currentLine=131079;
 //BA.debugLineNum = 131079;BA.debugLine="SplashLogo.BringToFront";
mostCurrent._splashlogo.BringToFront();
RDebugUtils.currentLine=131080;
 //BA.debugLineNum = 131080;BA.debugLine="PanelCarta.SendToBack";
mostCurrent._panelcarta.SendToBack();
RDebugUtils.currentLine=131081;
 //BA.debugLineNum = 131081;BA.debugLine="PanelSombra.SendToBack";
mostCurrent._panelsombra.SendToBack();
RDebugUtils.currentLine=131082;
 //BA.debugLineNum = 131082;BA.debugLine="PanelRojo.Visible = False";
mostCurrent._panelrojo.setVisible(anywheresoftware.b4a.keywords.Common.False);
RDebugUtils.currentLine=131083;
 //BA.debugLineNum = 131083;BA.debugLine="LabelSeleccion.Visible = False";
mostCurrent._labelseleccion.setVisible(anywheresoftware.b4a.keywords.Common.False);
RDebugUtils.currentLine=131084;
 //BA.debugLineNum = 131084;BA.debugLine="PanelLogin.Visible = False";
mostCurrent._panellogin.setVisible(anywheresoftware.b4a.keywords.Common.False);
RDebugUtils.currentLine=131085;
 //BA.debugLineNum = 131085;BA.debugLine="End Sub";
return "";
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
RDebugUtils.currentModule="main";
RDebugUtils.currentLine=262144;
 //BA.debugLineNum = 262144;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
RDebugUtils.currentLine=262145;
 //BA.debugLineNum = 262145;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
RDebugUtils.currentModule="main";
if (Debug.shouldDelegate(mostCurrent.activityBA, "activity_resume"))
	return (String) Debug.delegate(mostCurrent.activityBA, "activity_resume", null);
RDebugUtils.currentLine=196608;
 //BA.debugLineNum = 196608;BA.debugLine="Sub Activity_Resume";
RDebugUtils.currentLine=196609;
 //BA.debugLineNum = 196609;BA.debugLine="Animaciones.Initialize(False)";
mostCurrent._animaciones.Initialize(anywheresoftware.b4a.keywords.Common.False);
RDebugUtils.currentLine=196610;
 //BA.debugLineNum = 196610;BA.debugLine="AnimacionArriba.InitializeTranslate(\"AnimacionArri";
mostCurrent._animacionarriba.InitializeTranslate(mostCurrent.activityBA,"AnimacionArriba",(float) (0),(float) (0),(float) (0),(float) (-anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA)));
RDebugUtils.currentLine=196611;
 //BA.debugLineNum = 196611;BA.debugLine="Animaciones.AddAnimation(AnimacionArriba)";
mostCurrent._animaciones.AddAnimation(mostCurrent._animacionarriba);
RDebugUtils.currentLine=196612;
 //BA.debugLineNum = 196612;BA.debugLine="Animaciones.Duration = 1000";
mostCurrent._animaciones.setDuration((long) (1000));
RDebugUtils.currentLine=196613;
 //BA.debugLineNum = 196613;BA.debugLine="Animaciones.StartOffset = 1500";
mostCurrent._animaciones.setStartOffset((long) (1500));
RDebugUtils.currentLine=196614;
 //BA.debugLineNum = 196614;BA.debugLine="Animaciones.PersistAfter = True";
mostCurrent._animaciones.setPersistAfter(anywheresoftware.b4a.keywords.Common.True);
RDebugUtils.currentLine=196615;
 //BA.debugLineNum = 196615;BA.debugLine="Animaciones.Start(SplashLogo)";
mostCurrent._animaciones.Start((android.view.View)(mostCurrent._splashlogo.getObject()));
RDebugUtils.currentLine=196616;
 //BA.debugLineNum = 196616;BA.debugLine="End Sub";
return "";
}
public static String  _animacionarriba_animationend() throws Exception{
RDebugUtils.currentModule="main";
if (Debug.shouldDelegate(mostCurrent.activityBA, "animacionarriba_animationend"))
	return (String) Debug.delegate(mostCurrent.activityBA, "animacionarriba_animationend", null);
RDebugUtils.currentLine=327680;
 //BA.debugLineNum = 327680;BA.debugLine="Sub AnimacionArriba_AnimationEnd";
RDebugUtils.currentLine=327682;
 //BA.debugLineNum = 327682;BA.debugLine="PanelLogin.BringToFront";
mostCurrent._panellogin.BringToFront();
RDebugUtils.currentLine=327683;
 //BA.debugLineNum = 327683;BA.debugLine="PanelLogin.Visible = True";
mostCurrent._panellogin.setVisible(anywheresoftware.b4a.keywords.Common.True);
RDebugUtils.currentLine=327684;
 //BA.debugLineNum = 327684;BA.debugLine="LabelSeleccion.BringToFront";
mostCurrent._labelseleccion.BringToFront();
RDebugUtils.currentLine=327685;
 //BA.debugLineNum = 327685;BA.debugLine="LabelSeleccion.Visible = True";
mostCurrent._labelseleccion.setVisible(anywheresoftware.b4a.keywords.Common.True);
RDebugUtils.currentLine=327686;
 //BA.debugLineNum = 327686;BA.debugLine="PanelRojo.BringToFront";
mostCurrent._panelrojo.BringToFront();
RDebugUtils.currentLine=327687;
 //BA.debugLineNum = 327687;BA.debugLine="PanelRojo.Visible = True";
mostCurrent._panelrojo.setVisible(anywheresoftware.b4a.keywords.Common.True);
RDebugUtils.currentLine=327688;
 //BA.debugLineNum = 327688;BA.debugLine="Usuarios.Visible = True";
mostCurrent._usuarios.setVisible(anywheresoftware.b4a.keywords.Common.True);
RDebugUtils.currentLine=327689;
 //BA.debugLineNum = 327689;BA.debugLine="PanelRojo.Visible = True";
mostCurrent._panelrojo.setVisible(anywheresoftware.b4a.keywords.Common.True);
RDebugUtils.currentLine=327691;
 //BA.debugLineNum = 327691;BA.debugLine="AnimacionFade.InitializeAlpha(\"AnimacionFade\", 0,";
mostCurrent._animacionfade.InitializeAlpha(mostCurrent.activityBA,"AnimacionFade",(float) (0),(float) (1));
RDebugUtils.currentLine=327692;
 //BA.debugLineNum = 327692;BA.debugLine="AnimacionFadeRojo.InitializeAlpha(\"AnimacionFadeR";
mostCurrent._animacionfaderojo.InitializeAlpha(mostCurrent.activityBA,"AnimacionFadeRojo",(float) (0),(float) (1));
RDebugUtils.currentLine=327693;
 //BA.debugLineNum = 327693;BA.debugLine="AnimacionFadeRojo.Duration = 1000";
mostCurrent._animacionfaderojo.setDuration((long) (1000));
RDebugUtils.currentLine=327694;
 //BA.debugLineNum = 327694;BA.debugLine="AnimacionFade.Duration = 1000";
mostCurrent._animacionfade.setDuration((long) (1000));
RDebugUtils.currentLine=327695;
 //BA.debugLineNum = 327695;BA.debugLine="AnimacionFadeRojo.Start(PanelRojo)";
mostCurrent._animacionfaderojo.Start((android.view.View)(mostCurrent._panelrojo.getObject()));
RDebugUtils.currentLine=327696;
 //BA.debugLineNum = 327696;BA.debugLine="AnimacionFade.Start(PanelLogin)";
mostCurrent._animacionfade.Start((android.view.View)(mostCurrent._panellogin.getObject()));
RDebugUtils.currentLine=327697;
 //BA.debugLineNum = 327697;BA.debugLine="End Sub";
return "";
}
public static String  _animacionfade_animationend() throws Exception{
RDebugUtils.currentModule="main";
if (Debug.shouldDelegate(mostCurrent.activityBA, "animacionfade_animationend"))
	return (String) Debug.delegate(mostCurrent.activityBA, "animacionfade_animationend", null);
RDebugUtils.currentLine=393216;
 //BA.debugLineNum = 393216;BA.debugLine="Sub AnimacionFade_AnimationEnd";
RDebugUtils.currentLine=393217;
 //BA.debugLineNum = 393217;BA.debugLine="End Sub";
return "";
}
public static String  _botonacceder_click() throws Exception{
RDebugUtils.currentModule="main";
if (Debug.shouldDelegate(mostCurrent.activityBA, "botonacceder_click"))
	return (String) Debug.delegate(mostCurrent.activityBA, "botonacceder_click", null);
RDebugUtils.currentLine=917504;
 //BA.debugLineNum = 917504;BA.debugLine="Sub BotonAcceder_Click";
RDebugUtils.currentLine=917505;
 //BA.debugLineNum = 917505;BA.debugLine="If Usuarios.SelectedIndex = 0 Then";
if (mostCurrent._usuarios.getSelectedIndex()==0) { 
RDebugUtils.currentLine=917506;
 //BA.debugLineNum = 917506;BA.debugLine="Password = Input.Show(\"\", \"Escriba la contraseña";
mostCurrent._password = BA.NumberToString(mostCurrent._input.Show("","Escriba la contraseña","Aceptar","Cancelar","",mostCurrent.activityBA,(android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.Null)));
RDebugUtils.currentLine=917507;
 //BA.debugLineNum = 917507;BA.debugLine="If Password = -1 Then";
if ((mostCurrent._password).equals(BA.NumberToString(-1))) { 
RDebugUtils.currentLine=917508;
 //BA.debugLineNum = 917508;BA.debugLine="ToastMessageShow(Input.Input ,False)";
anywheresoftware.b4a.keywords.Common.ToastMessageShow(mostCurrent._input.getInput(),anywheresoftware.b4a.keywords.Common.False);
 };
 }else {
RDebugUtils.currentLine=917511;
 //BA.debugLineNum = 917511;BA.debugLine="ToastMessageShow(\"Accediste como: \" & Usuarios.";
anywheresoftware.b4a.keywords.Common.ToastMessageShow("Accediste como: "+mostCurrent._usuarios.getSelectedItem(),anywheresoftware.b4a.keywords.Common.False);
 };
RDebugUtils.currentLine=917513;
 //BA.debugLineNum = 917513;BA.debugLine="End Sub";
return "";
}
public static String  _botoncancelar_click() throws Exception{
RDebugUtils.currentModule="main";
if (Debug.shouldDelegate(mostCurrent.activityBA, "botoncancelar_click"))
	return (String) Debug.delegate(mostCurrent.activityBA, "botoncancelar_click", null);
RDebugUtils.currentLine=983040;
 //BA.debugLineNum = 983040;BA.debugLine="Sub BotonCancelar_Click";
RDebugUtils.currentLine=983041;
 //BA.debugLineNum = 983041;BA.debugLine="Activity.Finish";
mostCurrent._activity.Finish();
RDebugUtils.currentLine=983042;
 //BA.debugLineNum = 983042;BA.debugLine="End Sub";
return "";
}
}
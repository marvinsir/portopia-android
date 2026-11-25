package org.retrocase.engine;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MainActivity extends Activity {
    static final int PICK=101, BG=0xff111713, FG=0xffcce8bf, DIM=0xff263a2c;
    static final String[] LANG={"ja","zh-Hans","zh-Hant","en"};
    String lang="zh-Hant", scene, message="";
    JSONObject game; JSONObject messageObject;
    final Set<String> flags=new LinkedHashSet<>();
    SharedPreferences prefs; boolean imported=false;
    String tr(String ja,String cn,String tw,String en){return lang.equals("en")?en:lang.equals("ja")?ja:lang.equals("zh-Hant")?tw:cn;}
    String loc(JSONObject o){if(o==null)return "";return o.optString(lang,o.optString("ja",o.optString("zh-Hans","")));}
    int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("retrocase",MODE_PRIVATE);lang=prefs.getString("lang","zh-Hant");
        try {File f=new File(getFilesDir(),"case.json");imported=f.isFile();game=parse(imported?new FileInputStream(f):getAssets().open("demo.json"));validate(game);restore();}
        catch(Exception e){try{imported=false;game=parse(getAssets().open("demo.json"));validate(game);reset();}catch(Exception fatal){throw new IllegalStateException(fatal);}} render();}
    JSONObject parse(InputStream in)throws Exception{try(InputStream src=in;ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=src.read(b))!=-1){if(out.size()+n>4*1024*1024)throw new IOException("JSON exceeds 4 MiB");out.write(b,0,n);}return new JSONObject(new String(out.toByteArray(),StandardCharsets.UTF_8));}}
    void checkText(JSONObject o)throws JSONException{for(String l:LANG)if(o.getString(l).trim().isEmpty())throw new JSONException("Empty translation: "+l);}
    void validate(JSONObject g)throws JSONException{if(g.getInt("schema")!=1)throw new JSONException("Unsupported schema");checkText(g.getJSONObject("title"));JSONObject s=g.getJSONObject("scenes");if(!s.has(g.getString("start")))throw new JSONException("Missing start");Iterator<String> it=s.keys();while(it.hasNext()){String id=it.next();JSONObject v=s.getJSONObject(id);checkText(v.getJSONObject("title"));checkText(v.getJSONObject("text"));JSONArray aa=v.getJSONArray("actions");for(int i=0;i<aa.length();i++){JSONObject a=aa.getJSONObject(i);checkText(a.getJSONObject("label"));if(a.has("reply"))checkText(a.getJSONObject("reply"));if(a.has("goto")&&!s.has(a.getString("goto")))throw new JSONException("Unknown scene: "+a.getString("goto"));for(String k:new String[]{"requires","forbids","set","clear"})if(a.has(k)){JSONArray arr=a.getJSONArray(k);for(int j=0;j<arr.length();j++)arr.getString(j);}}}}
    JSONObject current(){return game.optJSONObject("scenes").optJSONObject(scene);}
    void reset(){scene=game.optString("start");flags.clear();messageObject=null;save();}
    void restore(){scene=prefs.getString("scene",game.optString("start"));if(current()==null)scene=game.optString("start");flags.clear();flags.addAll(prefs.getStringSet("flags",Collections.emptySet()));try{messageObject=new JSONObject(prefs.getString("reply","{}"));if(messageObject.length()==0)messageObject=null;}catch(Exception e){messageObject=null;}}
    void save(){prefs.edit().putString("lang",lang).putString("scene",scene).putStringSet("flags",new LinkedHashSet<>(flags)).putString("reply",messageObject==null?"{}":messageObject.toString()).apply();}
    boolean allowed(JSONObject a){JSONArray r=a.optJSONArray("requires"),f=a.optJSONArray("forbids");if(r!=null)for(int i=0;i<r.length();i++)if(!flags.contains(r.optString(i)))return false;if(f!=null)for(int i=0;i<f.length();i++)if(flags.contains(f.optString(i)))return false;return true;}
    void act(JSONObject a){if(!allowed(a))return;for(String k:new String[]{"set","clear"}){JSONArray arr=a.optJSONArray(k);if(arr!=null)for(int i=0;i<arr.length();i++){String v=arr.optString(i);if(k.equals("set"))flags.add(v);else flags.remove(v);}}scene=a.optString("goto",scene);messageObject=a.optJSONObject("reply");save();render();}
    TextView text(String s,int size){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(FG);v.setPadding(dp(8),dp(5),dp(8),dp(5));return v;}
    Button btn(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setTextColor(FG);b.setAllCaps(false);b.setTextSize(15);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(DIM));b.setOnClickListener(v->r.run());return b;}
    LinearLayout vertical(){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);return x;}
    void render(){JSONObject s=current();LinearLayout root=vertical();root.setBackgroundColor(BG);root.setPadding(dp(12),dp(8),dp(12),dp(8));
        if(android.os.Build.VERSION.SDK_INT>=35){root.setOnApplyWindowInsetsListener((v,in)->{android.graphics.Insets n=in.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());v.setPadding(n.left+dp(12),n.top+dp(8),n.right+dp(12),n.bottom+dp(8));return in;});}
        root.addView(text(loc(game.optJSONObject("title")),20));root.addView(text(imported?tr("読み込みデータ","已导入数据","已匯入資料","Imported case"):tr("独自テストシナリオ・原作未収録","独立测试剧情 · 未包含原作","獨立測試劇情 · 未收錄原作","Independent demo · Original game not included"),12));
        LinearLayout bar=new LinearLayout(this);bar.addView(btn(tr("言語","语言","語言","Language"),this::language),new LinearLayout.LayoutParams(0,-2,1));bar.addView(btn(tr("読込","导入","匯入","Import"),this::pick),new LinearLayout.LayoutParams(0,-2,1));bar.addView(btn(tr("最初から","重开","重玩","Restart"),()->new AlertDialog.Builder(this).setMessage(tr("最初から？","重新开始？","重新開始？","Restart the case?")).setPositiveButton("OK",(d,w)->{reset();render();}).setNegativeButton(android.R.string.cancel,null).show()),new LinearLayout.LayoutParams(0,-2,1));root.addView(bar);
        boolean landscape=getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE;
        LinearLayout body=new LinearLayout(this);body.setOrientation(landscape?LinearLayout.HORIZONTAL:LinearLayout.VERTICAL);root.addView(body,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout left=vertical();body.addView(left,landscape?new LinearLayout.LayoutParams(0,-1,3):new LinearLayout.LayoutParams(-1,0,1));left.addView(text(loc(s.optJSONObject("title")),18));SceneView art=new SceneView(s.optString("art","room"));left.addView(art,new LinearLayout.LayoutParams(-1,0,1));
        ScrollView dialogue=new ScrollView(this);TextView words=text(loc(messageObject==null?s.optJSONObject("text"):messageObject),17);words.setTextIsSelectable(true);dialogue.addView(words);left.addView(dialogue,new LinearLayout.LayoutParams(-1,dp(landscape?90:105)));
        ScrollView menu=new ScrollView(this);LinearLayout actions=vertical();menu.addView(actions);body.addView(menu,landscape?new LinearLayout.LayoutParams(0,-1,2):new LinearLayout.LayoutParams(-1,0,1));JSONArray aa=s.optJSONArray("actions");for(int i=0;i<aa.length();i++){JSONObject a=aa.optJSONObject(i);if(allowed(a))actions.addView(btn(loc(a.optJSONObject("label")),()->act(a)));}root.addView(text(tr("自動保存 · オフライン","自动存档 · 完全离线","自動存檔 · 完全離線","Auto-save · Offline"),11));setContentView(root);root.requestApplyInsets();}
    @Override public void onConfigurationChanged(Configuration c){super.onConfigurationChanged(c);render();}
    void language(){new AlertDialog.Builder(this).setTitle(tr("言語","语言","語言","Language")).setItems(new String[]{"日本語","简体中文","繁體中文","English"},(d,w)->{lang=LANG[w];save();render();}).show();}
    void pick(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,PICK);}
    @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(req!=PICK||result!=RESULT_OK||data==null||data.getData()==null)return;try{JSONObject next=parse(getContentResolver().openInputStream(data.getData()));validate(next);new AlertDialog.Builder(this).setMessage(tr("現在の事件と進行を置き換えますか？","替换当前游戏与存档？","取代目前遊戲與存檔？","Replace the current case and saved progress?")).setPositiveButton("OK",(d,w)->{try{android.util.AtomicFile f=new android.util.AtomicFile(new File(getFilesDir(),"case.json"));FileOutputStream out=f.startWrite();try{out.write(next.toString().getBytes(StandardCharsets.UTF_8));f.finishWrite(out);}catch(Exception e){f.failWrite(out);throw e;}game=next;imported=true;reset();render();}catch(Exception e){error(e);}}).setNegativeButton(android.R.string.cancel,null).show();}catch(Exception e){error(e);}}
    void error(Exception e){new AlertDialog.Builder(this).setTitle(tr("エラー","错误","錯誤","Error")).setMessage(e.getMessage()).setPositiveButton("OK",null).show();}
    class SceneView extends View{final Paint p=new Paint();final String art;SceneView(String a){super(MainActivity.this);art=a;setContentDescription(tr("テスト用イラスト","测试插画","測試插圖","Demo illustration"));}void line(Canvas c,float... xy){for(int i=0;i+3<xy.length;i+=2)c.drawLine(xy[i],xy[i+1],xy[i+2],xy[i+3],p);}
        @Override protected void onDraw(Canvas c){super.onDraw(c);c.drawColor(0xff080d09);float scale=Math.min(getWidth()/320f,getHeight()/200f);c.save();c.translate((getWidth()-320*scale)/2,(getHeight()-200*scale)/2);c.scale(scale,scale);p.setColor(0xff87ba91);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);c.drawRect(5,5,315,195,p);if(art.equals("street")){line(c,12,155,100,92,190,92,308,155);line(c,25,140,25,50,87,50,87,100);line(c,220,110,220,35,290,35,290,145);for(int y=48;y<95;y+=20){c.drawRect(232,y,247,y+10,p);c.drawRect(260,y,275,y+10,p);}}else{line(c,15,150,75,98,245,98,305,150);line(c,75,98,75,15);line(c,245,98,245,15);c.drawRect(120,30,199,78,p);line(c,159,30,159,78);line(c,120,54,199,54);line(c,85,125,230,125,250,150,65,150,85,125);line(c,65,150,65,177);line(c,250,150,250,177);c.drawRect(135,131,178,143,p);}p.setColor(0x2233aa55);p.setStrokeWidth(.5f);for(int y=7;y<194;y+=3)c.drawLine(6,y,314,y,p);c.restore();}}
}

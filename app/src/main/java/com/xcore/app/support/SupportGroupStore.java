package com.xcore.app.support;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class SupportGroupStore {
    private final SharedPreferences prefs;
    public SupportGroupStore(Context c){ prefs=c.getSharedPreferences("xcore_support_groups",Context.MODE_PRIVATE); }

    public List<SupportGroup> list(){
        List<SupportGroup> out=new ArrayList<>();
        try{
            JSONArray a=new JSONArray(prefs.getString("groups","[]"));
            for(int i=0;i<a.length();i++){
                JSONObject o=a.getJSONObject(i);
                SupportGroup.Unit u;
                try{u=SupportGroup.Unit.valueOf(o.optString("unit","MINUTES"));}catch(Exception e){u=SupportGroup.Unit.MINUTES;}
                out.add(new SupportGroup(o.optString("id"),o.optString("groupName"),o.optString("message"),
                        Math.max(1,o.optLong("interval",30)),u,o.optBoolean("active",true)));
            }
        }catch(Exception ignored){}
        return out;
    }
    public void save(SupportGroup g){
        List<SupportGroup> all=list(); boolean replaced=false;
        for(int i=0;i<all.size();i++) if(all.get(i).getId().equals(g.getId())){all.set(i,g);replaced=true;break;}
        if(!replaced) all.add(g); persist(all);
    }
    public void remove(String id){List<SupportGroup> all=list();all.removeIf(g->g.getId().equals(id));persist(all);}
    private void persist(List<SupportGroup> all){
        JSONArray a=new JSONArray();
        try{for(SupportGroup g:all){JSONObject o=new JSONObject();o.put("id",g.getId());o.put("groupName",g.getGroupName());o.put("message",g.getMessage());o.put("interval",g.getInterval());o.put("unit",g.getUnit().name());o.put("active",g.isActive());a.put(o);}}catch(Exception ignored){}
        prefs.edit().putString("groups",a.toString()).apply();
    }
}

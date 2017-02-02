package com.example.android.bluetoothchat.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by GyungDal on 2017-01-11.
 */
public class DBManager {

    // DB관련 상수 선언
    private static final String dbName = "data.db";
    public static final int dbVersion = 1;

    // DB관련 객체 선언
    private OpenHelper opener; // DB opener
    private SQLiteDatabase db; // DB controller

    // 부가적인 객체들
    private Context context;

    // 생성자
    public DBManager(Context context) {
        this.context = context;
        this.opener = new OpenHelper(context, dbName, null, dbVersion);
        db = opener.getWritableDatabase();
    }

    // Opener of DB and Table
    private class OpenHelper extends SQLiteOpenHelper {

        OpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version) {
            super(context, name, null, version);
            // TODO Auto-generated constructor stub
        }

        // 생성된 DB가 없을 경우에 한번만 호출됨
        @Override
        public void onCreate(SQLiteDatabase arg0) {
            String createSql = "create table number (name text, number text);";
            arg0.execSQL(createSql);
            createSql = "create table data (number text, address text);";
            arg0.execSQL(createSql);
            Toast.makeText(context, "DB is opened", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
        }
    }

    public void insertNumber(String name, String number){
        String sql = "insert into number values(\'" + name + "\', \'" + number + "\')";
        db.execSQL(sql);
    }

    public ArrayList<HashMap<String, String>> selectNumber(){
        String sql = "select * from number;";
        Cursor results = db.rawQuery(sql, null);

        results.moveToFirst();
        ArrayList<HashMap<String, String>> infos =  new ArrayList<HashMap<String, String>>();

        while (!results.isAfterLast()) {
            HashMap<String, String> map = new HashMap<String, String> ();
            map.put("item1", results.getString(0));
            map.put("item2", results.getString(1));
            infos.add(map);
            results.moveToNext();
        }
        results.close();
        return infos;
    }

    public void deleteNumber(String number){
        String sql = "delete from number where number = \'" + number + "\'";
        db.execSQL(sql);
    }

    // 데이터 추가
    public void insertData(String number, String address) {
        String sql = "insert into data values(\'" +
                number + "\', \'" + address + "\') on duplicate key update" +
                " number=\'" + number + "\', address=\'" + address + "\';";
        db.execSQL(sql);
    }

    // 데이터 삭제
    public void removeData(String address) {
        String sql = "delete from data where address = \'" + address + "\';";
        db.execSQL(sql);
    }


    // 데이터 전체 검색
    public ArrayList<Pair<String, String>> selectAll() {
        String sql = "select * from data;";
        Cursor results = db.rawQuery(sql, null);

        results.moveToFirst();
        ArrayList<Pair<String, String>> infos = new ArrayList<Pair<String, String>>();
        while (!results.isAfterLast()) {
            Pair<String, String> info = new Pair<String, String>(results.getString(0), results.getString(1));
            infos.add(info);
            results.moveToNext();
        }
        results.close();
        return infos;
    }
}
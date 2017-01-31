package com.example.android.bluetoothchat.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.android.bluetoothchat.MainActivity;
import com.example.android.bluetoothchat.R;
import com.example.android.bluetoothchat.utils.DBManager;
import com.example.android.common.logger.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by GyungDal on 2017-01-26.
 */

public class SosManagerFragment extends Fragment {
    private final int REQUEST_SELECT_CONTACT = 1;

    private ListView sosList;
    private ImageButton add;
    private DBManager dbManager;
    private ArrayList<HashMap<String, String>> numbers;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        dbManager = new DBManager(getContext());
        return inflater.inflate(R.layout.fragment_sos_manager, container, false);
    }


    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_CONTACT :
                if(data.getData() == null)
                    break;
                Cursor cursor = getContext().getContentResolver().query(data.getData(),
                        new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
                cursor.moveToFirst();
                String name = cursor.getString(0);        //0은 이름을 얻어옵니다.
                String number = cursor.getString(1);   //1은 번호를 받아옵니다.
                cursor.close();
                DBManager dbManager = new DBManager(getContext());
                Log.i("name", name);
                Log.i("number", number);
                Toast.makeText(getContext(), "name : " + name + "\nnumber : " + number,
                        Toast.LENGTH_SHORT).show();
                dbManager.insertNumber(name, number);
                numbers = dbManager.selectNumber();
                sosList.setAdapter(new SimpleAdapter(getActivity(), numbers, android.R.layout.simple_list_item_2,
                        new String[]{"item1", "item2"},
                        new int[]{android.R.id.text1, android.R.id.text2}));
                break;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        sosList = (ListView)view.findViewById(R.id.sos_list);
        add = (ImageButton)view.findViewById(R.id.add_button);
        numbers = dbManager.selectNumber();
        sosList.setAdapter(new SimpleAdapter(getActivity(), numbers, android.R.layout.simple_list_item_2,
                new String[]{"item1", "item2"},
                new int[]{android.R.id.text1, android.R.id.text2}));
        sosList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.myDialog));
                builder.setCancelable(true);
                builder.setMessage("삭제하시겟습니까?");
                builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        dbManager.deleteNumber(numbers.get(position).get("item2"));
                        numbers = dbManager.selectNumber();
                        sosList.setAdapter(new SimpleAdapter(getActivity(), (List<? extends Map<String, ?>>) numbers, android.R.layout.simple_list_item_2,
                                new String[]{"item1", "item2"},
                                new int[]{android.R.id.text1, android.R.id.text2}));
                    }
                });
                builder.show();
                return false;
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(numbers.size() < 3) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                    startActivityForResult(intent, REQUEST_SELECT_CONTACT);
                }else{
                    Toast.makeText(getContext(), "FAIL...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

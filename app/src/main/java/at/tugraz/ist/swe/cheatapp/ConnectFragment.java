package at.tugraz.ist.swe.cheatapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class ConnectFragment extends Fragment {
    private MainActivity activity;
    private View view;
    private ListView listView;
    private Button connectButton;
    private ArrayAdapter<String> adapter;
    private int selectedListIndex = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_connect, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (MainActivity) getActivity();

        listView = view.findViewById(R.id.lv_con_devices);
        connectButton = view.findViewById(R.id.bt_con_connect);


        this.updateValues();

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setSelector(R.color.colorHighlight);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedListIndex = position;
            }
        });

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedListIndex < 0) {
                    Toast.makeText(view.getContext(), "No device selected.", Toast.LENGTH_LONG).show();
                } else {
                    activity.getBluetoothProvider().connectToDevice(activity.getBluetoothProvider().getPairedDevices().get(selectedListIndex));
                }
            }
        });
    }

    public void updateValues() {
        final List<Device> deviceList = activity.getBluetoothProvider().getPairedDevices();
        List<String> deviceIDs = getDeviceIDStringList(deviceList);

        if (this.adapter == null) {
            adapter = new ArrayAdapter<>(view.getContext(),
                    android.R.layout.simple_list_item_1, android.R.id.text1, deviceIDs);
            listView.setAdapter(adapter);
        } else {
            adapter.clear();
            adapter.addAll(deviceIDs);
            adapter.notifyDataSetChanged();
        }

    }

    private List<String> getDeviceIDStringList(List<Device> deviceList) {
        List<String> idList = new ArrayList<>();
        for (Device device : deviceList) {
            idList.add(device.getID());
        }

        return idList;
    }
}
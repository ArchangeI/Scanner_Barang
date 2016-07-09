package com.example.lanch.scanner.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.example.lanch.scanner.MyApplication;
import com.example.lanch.scanner.R;
import com.example.lanch.scanner.dialogFragment.CameraSelectorDialogFragment;
import com.example.lanch.scanner.dialogFragment.FormatSelectorDialogFragment;
import com.example.lanch.scanner.dialogFragment.MessageDialogFragment;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanAnak extends BaseScannerActivity implements MessageDialogFragment.MessageDialogListener,
        ZXingScannerView.ResultHandler, FormatSelectorDialogFragment.FormatSelectorDialogListener,
        CameraSelectorDialogFragment.CameraSelectorDialogListener
{
    private static final String FLASH_STATE = "FLASH_STATE";
    private static final String AUTO_FOCUS_STATE = "AUTO_FOCUS_STATE";
    private static final String SELECTED_FORMATS = "SELECTED_FORMATS";
    private static final String CAMERA_ID = "CAMERA_ID";
    private ZXingScannerView mScannerView;
    private boolean mFlash;
    private boolean mAutoFocus;
    private ArrayList<Integer> mSelectedIndices;
    private int mCameraId = -1;
    private String kids_id;
    ArrayList<String> hasilscan = new ArrayList<String>();

    @Override
    public void onCreate(Bundle state)
    {
        super.onCreate(state);
        if(state != null)
        {
            mFlash = state.getBoolean(FLASH_STATE, false);
            mAutoFocus = state.getBoolean(AUTO_FOCUS_STATE, true);
            mSelectedIndices = state.getIntegerArrayList(SELECTED_FORMATS);
            mCameraId = state.getInt(CAMERA_ID, -1);
        } else
        {
            mFlash = false;
            mAutoFocus = true;
            mSelectedIndices = null;
            mCameraId = -1;
        }

        setContentView(R.layout.activity_scan_anak);
        setupToolbar();
        Button submit = (Button) findViewById(R.id.button);
        submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                fetch();
            }
        });
        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.Scan);
        mScannerView = new ZXingScannerView(this);
        setupFormats();
        contentFrame.addView(mScannerView);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera(mCameraId);
        mScannerView.setFlash(mFlash);
        mScannerView.setAutoFocus(mAutoFocus);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FLASH_STATE, mFlash);
        outState.putBoolean(AUTO_FOCUS_STATE, mAutoFocus);
        outState.putIntegerArrayList(SELECTED_FORMATS, mSelectedIndices);
        outState.putInt(CAMERA_ID, mCameraId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem;

        if(mFlash)
        {
            menuItem = menu.add(Menu.NONE, R.id.menu_flash, 0, R.string.flash_on);
        } else {
            menuItem = menu.add(Menu.NONE, R.id.menu_flash, 0, R.string.flash_off);
        }
        MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_NEVER);


        if(mAutoFocus) {
            menuItem = menu.add(Menu.NONE, R.id.menu_auto_focus, 0, R.string.auto_focus_on);
        } else {
            menuItem = menu.add(Menu.NONE, R.id.menu_auto_focus, 0, R.string.auto_focus_off);
        }
        MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_NEVER);

        menuItem = menu.add(Menu.NONE, R.id.menu_formats, 0, R.string.formats);
        MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_NEVER);

        menuItem = menu.add(Menu.NONE, R.id.menu_camera_selector, 0, R.string.select_camera);
        MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_NEVER);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle presses on the action bar items
        switch (item.getItemId())
        {
            case R.id.menu_flash:
                mFlash = !mFlash;
                if(mFlash)
                {
                    item.setTitle(R.string.flash_on);
                } else
                {
                    item.setTitle(R.string.flash_off);
                }
                mScannerView.setFlash(mFlash);
                return true;
            case R.id.menu_auto_focus:
                mAutoFocus = !mAutoFocus;
                if(mAutoFocus)
                {
                    item.setTitle(R.string.auto_focus_on);
                }
                else
                {
                    item.setTitle(R.string.auto_focus_off);
                }
                mScannerView.setAutoFocus(mAutoFocus);
                return true;
            case R.id.menu_formats:
                DialogFragment fragment = FormatSelectorDialogFragment.newInstance(this, mSelectedIndices);
                fragment.show(getSupportFragmentManager(), "format_selector");
                return true;
            case R.id.menu_camera_selector:
                mScannerView.stopCamera();
                DialogFragment cFragment = CameraSelectorDialogFragment.newInstance(this, mCameraId);
                cFragment.show(getSupportFragmentManager(), "camera_selector");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void handleResult(Result rawResult)
    {
        Log.d("Result", rawResult.getText());
        //kids_id = rawResult.getText().trim();
        hasilscan.add(rawResult.getText().trim());

       // fetch();
    }

    public void fetch()
    {
        String url = "http://kingdomkidz.site88.net/read_one.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d("response", response);

                try
                {
                    JSONObject jsonObject = new JSONObject(response);
                    int success = jsonObject.getInt("success");
                    Log.d("Result", String.valueOf(success));
                    if(success == 1)
                    {
                        JSONArray jsonArray = jsonObject.getJSONArray("student");

                        JSONObject jsonRow = jsonArray.getJSONObject(0);
                        Log.d("Json_response",jsonArray.toString());

                        if(jsonRow.has("waktu_masuk"))
                        {
                            loadPhoto(jsonRow.getString("image"), "Nama : " +
                                    jsonRow.getString("Name") + "\n" + "Jam Masuk : " +
                                    jsonRow.getString("waktu_masuk"));

                        }
                        else if(jsonRow.has("waktu_keluar"))
                        {
                            loadPhoto(jsonRow.getString("image"), "Nama : " +
                                    jsonRow.getString("Name") + "\n" + "Jam Keluar : " +
                                    jsonRow.getString("waktu_keluar"));
                        }


                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Something happened", Toast.LENGTH_LONG).show();
                    }
                }
                catch(JSONException e)
                {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Anak Sudah 3 kali discan", Toast.LENGTH_SHORT).show();
                    mScannerView.resumeCameraPreview(ScanAnak.this);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Toast.makeText(getApplicationContext(),"Error Happened", Toast.LENGTH_SHORT).show();
            }
        })
        {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();
                for (int i = 0; i < hasilscan.size(); i++) {
                    kids_id= hasilscan.get(i);

                    params.put("ID", kids_id);
                    return params;
                }
                return params;
            }
        };

        MyApplication.getInstance().addToReqQueue(stringRequest);
    }


    public void loadPhoto(String url, String absent)
    {
        AlertDialog.Builder imageDialog = new AlertDialog.Builder(this);

        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.custom_full_image, (ViewGroup)findViewById(R.id.layout_root));

        TextView textView = (TextView)layout.findViewById(R.id.custom_fullimage_placename);
        textView.setText(absent);

        NetworkImageView image = (NetworkImageView)layout.findViewById(R.id.photo);
        image.setErrorImageResId(R.drawable.fwd);
        image.setImageUrl(url, MyApplication.getInstance().getImageLoader());

        imageDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                mScannerView.resumeCameraPreview(ScanAnak.this);
            }
        });

        imageDialog.setTitle("Hasil");
        imageDialog.setView(layout);
        imageDialog.create();
        imageDialog.show();
    }



    public void showMessageDialog(String message)
    {
        DialogFragment fragment = MessageDialogFragment.newInstance("Scan Results", message, this);
        fragment.show(getSupportFragmentManager(), "scan_results");
    }

    public void closeMessageDialog() {
        closeDialog("scan_results");
    }

    public void closeFormatsDialog() {
        closeDialog("format_selector");
    }

    public void closeDialog(String dialogName)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DialogFragment fragment = (DialogFragment) fragmentManager.findFragmentByTag(dialogName);
        if(fragment != null)
        {
            fragment.dismiss();
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog)
    {
        // Resume the camera
        mScannerView.resumeCameraPreview(this);
    }

    @Override
    public void onFormatsSaved(ArrayList<Integer> selectedIndices)
    {
        mSelectedIndices = selectedIndices;
        setupFormats();
    }

    @Override
    public void onCameraSelected(int cameraId)
    {
        mCameraId = cameraId;
        mScannerView.startCamera(mCameraId);
        mScannerView.setFlash(mFlash);
        mScannerView.setAutoFocus(mAutoFocus);
    }

    public void setupFormats() {
        List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
        if(mSelectedIndices == null || mSelectedIndices.isEmpty())
        {
            mSelectedIndices = new ArrayList<Integer>();
            for(int i = 0; i < ZXingScannerView.ALL_FORMATS.size(); i++)
            {
                mSelectedIndices.add(i);
            }
        }

        for(int index : mSelectedIndices)
        {
            formats.add(ZXingScannerView.ALL_FORMATS.get(index));
        }
        if(mScannerView != null)
        {
            mScannerView.setFormats(formats);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mScannerView.stopCamera();
        closeMessageDialog();
        closeFormatsDialog();
    }
}

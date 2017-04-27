package com.excellence.skinloader;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.excellence.skinloader.skin.BaseActivity;

public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener
{
	private String[] mNames = new String[] { "当前APK内后缀换肤", "未安装的资源APK换肤", "已安装的APK换肤" };
	private GridView mGridView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mGridView = (GridView) findViewById(R.id.gridview);
		mGridView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mNames));
		mGridView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		switch (position)
		{
		case 0:
			startActivity(new Intent(MainActivity.this, SkinBySuffixActivity.class));
			break;

		case 1:
			startActivity(new Intent(MainActivity.this, SkinByAPKFileActivity.class));
			break;

		case 2:
			startActivity(new Intent(MainActivity.this, SkinByInstalledAPKActivity.class));
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (event.getAction() == KeyEvent.ACTION_DOWN)
		{
			switch (keyCode)
			{
			case KeyEvent.KEYCODE_BACK:
			case KeyEvent.KEYCODE_ESCAPE:
				android.os.Process.killProcess(android.os.Process.myPid());
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}
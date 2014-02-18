package bos.consoar.smartcircularmenu;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import bos.consoar.smartcircularmenu.SmartCircularMenu.onCircularClickListener;
import bos.consoar.smartcircularmenu.SmartCircularMenuItem.OnSmartCircularMenuPressed;

public class SmartCircularMenuActivity extends Activity {
	private SmartCircularMenu mMenu;
	private SmartCircularMenuItem mCamera, mGood, mInfo;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_circular_radial_menu);
		initMenu();
	}

	private void initMenu() {
		// TODO Auto-generated method stub

		mCamera = new SmartCircularMenuItem("camera", getResources()
				.getDrawable(R.drawable.ic_navigation_camera), "Camera");
		mGood = new SmartCircularMenuItem("good", getResources()
				.getDrawable(R.drawable.ic_navigation_good), "Good");
		mInfo = new SmartCircularMenuItem("info", getResources().getDrawable(
				R.drawable.ic_navigation_about), "Info");

		Button bt1=(Button)findViewById(R.id.button_fadeout);
		bt1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mMenu.setVisit(View.GONE);
			}
		});
		Button bt2=(Button)findViewById(R.id.button_fadein);
		bt2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mMenu.setVisit(View.VISIBLE);
			}
		});
		
		Button bt3=(Button)findViewById(R.id.button_anime);
		bt3.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mMenu.isAnimation())
					mMenu.setAnimation(false);
				else mMenu.setAnimation(true);
			}
		});
		mMenu = (SmartCircularMenu) findViewById(R.id.radial_menu);
		mMenu.addMenuItem(mCamera.getMenuID(), mCamera);
		mMenu.addMenuItem(mGood.getMenuID(), mGood);
		mMenu.setBitmap(BitmapFactory.decodeResource(getResources(),
				R.drawable.tt));
		mMenu.addMenuItem(mInfo.getMenuID(), mInfo);
		mMenu.setOrientation(SmartCircularMenu.HORIZONTAL_BOTTOM);
		mMenu.setOnCircularClickListener(new onCircularClickListener() {
			
			@Override
			public void onCircularButtonClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(SmartCircularMenuActivity.this,
						"单击Home", Toast.LENGTH_SHORT).show();
			}
		});
		mCamera.setOnSmartCircularMenuPressed(new OnSmartCircularMenuPressed() {
			@Override
			public void onMenuItemPressed() {
				Toast.makeText(SmartCircularMenuActivity.this,
						mCamera.getText(), Toast.LENGTH_LONG).show();
			}
		});

		mGood.setOnSmartCircularMenuPressed(new OnSmartCircularMenuPressed() {
			@Override
			public void onMenuItemPressed() {
				Toast.makeText(SmartCircularMenuActivity.this,
						mGood.getText(), Toast.LENGTH_LONG).show();
			}
		});

		mInfo.setOnSmartCircularMenuPressed(new OnSmartCircularMenuPressed() {
			@Override
			public void onMenuItemPressed() {
				Toast.makeText(SmartCircularMenuActivity.this, mInfo.getText(),
						Toast.LENGTH_LONG).show();
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

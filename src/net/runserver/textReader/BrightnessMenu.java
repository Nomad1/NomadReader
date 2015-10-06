package net.runserver.textReader;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

@SuppressWarnings("deprecation")
public class BrightnessMenu implements OnSeekBarChangeListener
{
	private final ViewGroup m_brightnessDialog;
	private final View m_brightnessTooltip;
	private final SeekBar m_brightnessSeeker;
	private final CheckBox m_brightnessSystem;
	private OnBrightnessChangeListener m_listener;

	public boolean isVisible()
	{
		return  m_brightnessDialog.getVisibility() == View.VISIBLE;
	}
	
	public ViewGroup getView()
	{
		return m_brightnessDialog;
	}
	
	public void setBrightnessChangeListener(OnBrightnessChangeListener listener)
	{
		m_listener = listener;		
	}
	
	public BrightnessMenu(ViewGroup brightnessDialog)
	{
		m_brightnessDialog = brightnessDialog;
		

		m_brightnessTooltip = m_brightnessDialog.findViewById(R.id.brightness_tip_container);
		m_brightnessSystem  = (CheckBox)m_brightnessDialog.findViewById(R.id.system);
		
		m_brightnessSystem.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean checked)
			{
				m_brightnessSeeker.setEnabled(!checked);
				if (checked)
					m_brightnessSeeker.setProgress(0);
			}
		});
		//brightnessSystem.
		
		m_brightnessSeeker = ((SeekBar)m_brightnessDialog.findViewById(R.id.slider));
		m_brightnessSeeker.setOnSeekBarChangeListener(this);
		m_brightnessSeeker.setThumb(brightnessDialog.getContext().getResources().getDrawable(R.drawable.scrubber_control_selector_holo));
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{
		m_brightnessTooltip.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{
		m_brightnessTooltip.setVisibility(View.INVISIBLE);
	}
	
	@Override
	public void onProgressChanged(SeekBar v, int progress, boolean fromUser)
	{
		float percent;
		if (m_brightnessSystem.isChecked())
			percent = -1;
		else
			percent = progress/(float)v.getMax();
		
		TextView brightnessTooltipText = (TextView)m_brightnessDialog.findViewById(R.id.brightness_tip);
		brightnessTooltipText.setText(Integer.toString((int)(percent * 100)) + "%");
		AbsoluteLayout.LayoutParams params = (AbsoluteLayout.LayoutParams)m_brightnessTooltip.getLayoutParams();		
		ViewGroup parent = (ViewGroup)m_brightnessTooltip.getParent();
		params.x = (int)((parent.getWidth() - m_brightnessTooltip.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight())  * percent);
		m_brightnessTooltip.setLayoutParams(params);
		
		if (m_listener != null)
			m_listener.onBrightnessChanged(percent);
	}
	
	public void show(int brightness)
	{
		m_brightnessDialog.setVisibility(View.VISIBLE);
		
		m_brightnessSeeker.setProgress(brightness*m_brightnessSeeker.getMax()/255);
		m_brightnessSystem.setChecked(brightness <= 0);
		if (brightness <= 0)
			m_brightnessSeeker.setEnabled(false);		
	}

	public void hide()
	{		
		m_brightnessDialog.setVisibility(View.GONE);
	}
	

	interface OnBrightnessChangeListener
	{
		public void onBrightnessChanged(float percent);
	}
}

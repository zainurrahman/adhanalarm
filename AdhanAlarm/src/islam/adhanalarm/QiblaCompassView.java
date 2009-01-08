package islam.adhanalarm;

import java.text.DecimalFormat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class QiblaCompassView extends View {
	private float directionNorth = 0.45f;
	private float directionQibla = 0.45f;
	private TextView bearingNorth;
	private TextView bearingQibla;
	private DecimalFormat df = new DecimalFormat("#.###");
	private Bitmap compassBackground;
	private Bitmap compassNeedle;
	private Matrix rotateNeedle;
	private int width = 240;
	private int height = 240;
	private float centre_x = width * 0.5f;
	private float centre_y = height * 0.5f;
	
	public QiblaCompassView(Context context) {
		super(context);
		initCompassView();
	}
	public QiblaCompassView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initCompassView();
	}
	public QiblaCompassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initCompassView();
	}
	
	private void initCompassView() {
		setMinimumHeight(240);
		setMinimumWidth(240);
		invalidate();
	}
	
	public void setDirections(float directionNorth, float directionQibla, TextView bearingNorth, TextView bearingQibla) {
		this.directionNorth = directionNorth;
		this.directionQibla = directionQibla;
		this.bearingNorth = bearingNorth;
		this.bearingQibla = bearingQibla;
		width = getWidth();
		height = getHeight();
		centre_x = width  * 0.5f;
		centre_y = height * 0.5f;
		compassBackground = BitmapFactory.decodeResource(getResources(), R.drawable.compass_background);
		compassNeedle = BitmapFactory.decodeResource(getResources(), R.drawable.compass_needle);
		rotateNeedle = new Matrix();
		rotateNeedle.postRotate(-directionQibla, compassNeedle.getWidth() * 0.5f, compassNeedle.getHeight() * 0.5f);
		rotateNeedle.postTranslate(centre_x - compassNeedle.getWidth() * 0.5f, -compassNeedle.getWidth() * 0.5f);
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		bearingNorth.setText(" " + df.format(directionNorth) + "\u00b0");
		bearingQibla.setText((directionQibla >= 0 ? " +" : " -") + " " + df.format(Math.abs(directionQibla)) + " = " + df.format(directionNorth + directionQibla) + "\u00b0");

		Paint p = new Paint();
		canvas.rotate(-directionNorth, centre_x, centre_y);
		canvas.drawBitmap(compassBackground, 0, 0, p);
		canvas.drawBitmap(compassNeedle, rotateNeedle, p);
	}
}
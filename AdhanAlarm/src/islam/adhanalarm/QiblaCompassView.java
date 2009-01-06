package islam.adhanalarm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class QiblaCompassView extends View {
	private float directionNorth;
	private float directionQibla;
	private TextView bearingNorth;
	private TextView bearingQibla;
	
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
		invalidate();
	}
	
	public void setDirections(float directionNorth, float directionQibla, TextView bearingNorth, TextView bearingQibla) {
		this.directionNorth = directionNorth;
		this.directionQibla = directionQibla;
		this.bearingNorth = bearingNorth;
		this.bearingQibla = bearingQibla;
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		Paint p = new Paint();
		bearingNorth.setText(" " + directionNorth + "\u00b0");
		bearingQibla.setText((directionQibla >= 0 ? " +" : " -") + " " + Math.abs(directionQibla) + " = " + (directionNorth + directionQibla) + "\u00b0");
		p.setColor(android.graphics.Color.WHITE);
		drawCompass(canvas, p);
	}
	
	private void drawCompass(Canvas canvas, Paint p) {
		p.setStrokeWidth(5);
		int width = getWidth();
		int height = getHeight();
		final float centre_x =  width * 0.5f;
		final float centre_y = height * 0.5f;
		canvas.rotate(-directionNorth, centre_x, centre_y);
		canvas.drawCircle(centre_x, centre_y, 10, p); // Center circle
		p.setColor(android.graphics.Color.argb(255, 57, 97, 11));
		canvas.drawCircle(centre_x, centre_y, centre_x, p); // Filled outer circle
		p.setColor(android.graphics.Color.WHITE);
		p.setStyle(android.graphics.Paint.Style.STROKE);
		canvas.drawCircle(centre_x, centre_y, centre_x, p); // Outer circle outline
		drawArrow(canvas, p,width, height);
	}
	private void drawArrow(Canvas canvas, Paint p, int width, int height) {
		final float centre_x = width  * 0.5f;
		final float centre_y = height * 0.5f;
		canvas.drawLine(centre_x, centre_y - (height*0.3f), centre_x, centre_y, p);
		drawArrowHead(canvas, p, width, height);
		p.setColor(android.graphics.Color.YELLOW);
		canvas.drawLine(centre_x - directionQibla, centre_y - (height*0.23f), centre_x, centre_y, p);
	}
	
	private void drawArrowHead(Canvas canvas, Paint p, int width, int height) {
		final float centre_x = width  * 0.5f;
		final float centre_y = height * 0.5f;
		final float arrow_width = 0.03f;
		final float arrow_sides = 2.8f;
		final float arrow_sharp = -2.0f;
		final float arrow_distance = -4.0f;
		canvas.drawLine(centre_x - width * arrow_width,                             centre_y - (height * 0.25f) + arrow_distance, centre_x + width * arrow_width,       centre_y - (height * 0.25f) + arrow_distance, p);
		canvas.drawLine(centre_x - width * arrow_width + arrow_sides + arrow_sharp, centre_y - (height * 0.25f) - arrow_sides,    centre_x + arrow_sides + arrow_sharp, centre_y - (height * 0.3f) - arrow_sides,     p);
		canvas.drawLine(centre_x + width * arrow_width - arrow_sides - arrow_sharp, centre_y - (height * 0.25f) - arrow_sides,    centre_x - arrow_sides - arrow_sharp, centre_y - (height * 0.3f) - arrow_sides,     p);
		// cheap way to fill the arrow head i.e. draw two more lines right next to the sides of the arrow head
		canvas.drawLine(centre_x - width * arrow_width + arrow_sides - arrow_sharp, centre_y - (height * 0.25f) - arrow_sides,    centre_x + arrow_sides + arrow_sharp, centre_y - (height * 0.3f) - arrow_sides,     p);
		canvas.drawLine(centre_x + width * arrow_width - arrow_sides + arrow_sharp, centre_y - (height * 0.25f) - arrow_sides,    centre_x - arrow_sides - arrow_sharp, centre_y - (height * 0.3f) - arrow_sides,     p);
	}
}
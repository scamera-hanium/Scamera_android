package com.example.davichiar.scamera_android.Search;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import java.util.List;

public class CircleChart extends android.support.v7.widget.AppCompatTextView {

    private boolean isLog = false;
    private Paint paint;
    private int paintWidth = 50;
    private int speed = 1;
    private int rotate = 0;
    private int startAngle = -90;
    private boolean isRun = false;
    private List<ChartData> list;
    private int space = paintWidth+20;
    private float centerX = 0,centerY = 0;
    private boolean autoSpace = true;
    private boolean textSlope = false;
    static int defaultColor = Color.RED , defaultStrokeColor = Color.BLACK , defaultTextColor = Color.BLACK,
            defultBackgroundColor = Color.LTGRAY , defultBackgroundStrokeColor = Color.DKGRAY;

    public CircleChart(Context context) {
        super(context);
    }

    public CircleChart(Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }

    public CircleChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        if(centerX == 0 || centerY == 0){
            centerX = getWidth()/2;
            centerY = getHeight()/2;
        }
        log("onDraw");
        if(list == null || list.size() == 0) return;
        if(isRun) {
            int count = 0;
            for(ChartData data : list){
                if(data.getRadius() == 0){
                    if(autoSpace) {
                        int space = (getWidth()/2-10)/list.size();
                        data.setRadius(centerX - centerX/8 - space * count);
                        count++;
                    }else{
                        data.setRadius(centerX - centerX/8 - space * count);
                        count++;
                    }
                }
                if(data.getPercentage() == 0) return;
                drawBackground(canvas,data.getBackgroundColor(),data.getBackgroundStrokeColor(),data.getRadius());
                drawArc(canvas, data.getPercentage(), data.getColor(),data.getStrokeColor(), data.getRadius(),data.getSpeed());
                drawText(canvas,data.getText(),paint.measureText(data.getText()),paintWidth/2,data.getTextColor(),data.getRadius());
            }
        }else{
            invalidate();
        }
    }

    private void drawArc(Canvas canvas, float percentage, int color, int strokeColor, float radius, int s){
        if((float)rotate/360*100 < percentage){
            drawArc(canvas,radius,startAngle,rotate,paintWidth+10,strokeColor);
            drawArc(canvas,radius,startAngle,rotate,paintWidth-1,color);
            rotate = rotate + s;
            log(rotate+"");
            invalidate();
        }else{
            drawArc(canvas,radius,startAngle,(int)(percentage/100*360),paintWidth+10,strokeColor);
            drawArc(canvas,radius,startAngle,(int)(percentage/100*360),paintWidth-1,color);
        }
    }

    private void drawArc(Canvas canvas,float radius,int angle,int rotate,int strokeWidth,int color){
        RectF oval = new RectF( centerX-radius, centerY-radius, centerX+radius, centerY+radius);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        canvas.drawArc(oval,angle,rotate+(-90-angle),false,paint);
    }

    private void drawBackground(Canvas canvas, int backgroundColor, int backgroundStrokeColor,float radius){
        RectF oval = new RectF( centerX-radius, centerY-radius,
                centerX+radius, centerY+radius);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(paintWidth+10);
        paint.setColor(backgroundStrokeColor);
        canvas.drawArc(oval,-90,360,false,paint);
        paint.setStrokeWidth(paintWidth);
        paint.setColor(backgroundColor);
        canvas.drawArc(oval,-90,360,false,paint);
    }

    private void drawText(Canvas canvas,String text,float textWidth, int textSize,int color,float radius){
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(textSize);
        if(textSlope && radius < getWidth()/4){
            RectF oval = new RectF( centerX-radius, centerY-radius, centerX+radius, centerY+radius);
            Path path = new Path();
            path.addArc(oval,-130,textWidth);
            canvas.drawTextOnPath(text,path,0,15,paint);
        }
        else{
            canvas.drawText(text,centerX-textWidth-getWidth()/20,centerY-radius+getWidth()/30,paint);
        }
    }

    public void setData(List<ChartData> list){
        this.list = list;
    }

    public void setDefaultColor(int color){
        defaultColor = color;
    }

    public void setDefaultTextColor(int color){
        defaultTextColor = color;
    }

    public void setSpeed(int speed){
        if(speed <= 0) return;
        this.speed = speed;
    }

    public void setStartAngle(int angle){
        this.startAngle = angle;
    }

    public void setPaintWidth(int width){
        if(width <= 0) return;
        paintWidth = width;
    }

    public void setSpace(int space){
        if(space <= 0) return;
        this.space = space;
        autoSpace = false;
    }

    public void setTextSlope(){
        textSlope = true;
    }

    public void run(){
        isRun = true;
        rotate = 0;
        invalidate();
    }

    public void openLog(){
        isLog = true;
    }

    private void log(String msg){
        if(isLog) Log.d("CircleChartTest",msg);
    }
}

class ChartData{
    private int percentage = 0;
    private int color = CircleChart.defaultColor;
    private int strokeColor = CircleChart.defaultColor;
    private int textColor = CircleChart.defaultTextColor;
    private int backgroundStrokeColor = CircleChart.defaultStrokeColor;
    private int backgroundColor = CircleChart.defultBackgroundColor;
    private int speed = 1;
    private String text = "data";
    private float radius = 0;

    public int getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getBackgroundStrokeColor() {
        return backgroundStrokeColor;
    }

    public void setBackgroundStrokeColor(int backgroundStrokeColor) {
        this.backgroundStrokeColor = backgroundStrokeColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
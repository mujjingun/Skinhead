package dimigo.org.skinhead;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.jtransforms.fft.FloatFFT_2D;

public class AnalyzeActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    public static String[] typename = {
            "공룡형",
            "2",
            "3",
            "4"
    };
    private ImageView imageView;
    private ProgressDialog progress;
    private TextView ageView, typeText;
    private int age;
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze);

        imageView = (ImageView) findViewById(R.id.imageView);
        ageView = (TextView) findViewById(R.id.age);
        typeText = (TextView) findViewById(R.id.typemsg);

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent i) {
        Log.i("debug", "onNewIntent");
        Bundle extras = i.getExtras();
        if (extras == null) return;
        Bitmap imageBitmap = (Bitmap) extras.get("data");
        if (imageBitmap != null) {
            setIntent(i);

            imageView.setImageBitmap(imageBitmap);

            progress = new ProgressDialog(this);
            progress.setTitle("분석 중...");
            progress.setMessage("피부를 분석하는 동안 기다려 주십시오...");
            progress.setCancelable(false);
            progress.show();

            analyze(imageBitmap);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        onNewIntent(data);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void onClickCamera(View v) {
        dispatchTakePictureIntent();
    }

    public void onClickView(View v) {
        Intent i = new Intent(this, ResultActivity.class);
        i.putExtra("age", age);
        i.putExtra("type", type);
        startActivity(i);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progress != null && progress.isShowing()) progress.dismiss();
    }

    private float[][] toRGB(Bitmap bitmap, int picw, int pich) {
        int[] pix = new int[picw * pich];
        bitmap.getPixels(pix, 0, picw, 0, 0, picw, pich);

        float[][] map = new float[picw][pich];

        for (int y = 0; y < pich; y++) {
            for (int x = 0; x < picw; x++) {
                int index = y * picw + x;
                int R = (pix[index] >> 16) & 0xff;     //bitwise shifting
                int G = (pix[index] >> 8) & 0xff;
                int B = pix[index] & 0xff;

                map[x][y] = (float) (R + G + B) / 3;
            }
        }
        return map;
    }

    private BitmapDrawable toBitmap(float[][] pixels, int picw, int pich) {
        int[] ret = new int[picw * pich];

        float[][] clamped = copyMat(pixels, picw, pich);
        clamp(clamped, picw, pich);

        for (int y = 0; y < pich; y++) {
            for (int x = 0; x < picw; x++) {
                int index = y * picw + x;
                int val = (int) (clamped[x][y]);
                ret[index] = 0xff000000 | (val << 16) | (val << 8) | val;
            }
        }
        Bitmap bit = Bitmap.createBitmap(ret, picw, pich, Bitmap.Config.ARGB_8888);
        BitmapDrawable bd = new BitmapDrawable(getResources(), bit);
        bd.setAntiAlias(false);
        return bd;
    }

    private void getMagnitude(float[][] pixels, int picw, int pich) {
        for (int y = 0; y < pich; y++) {
            for (int x = 0; x < picw; x++) {
                float a = pixels[y][2 * x];
                float b = pixels[y][2 * x + 1];
                float v = (float) Math.sqrt(a * a + b * b);
                pixels[x][y] = v;
            }
        }
    }

    private float hanning(float x, float N) {
        return (float) (.5 * (1 - Math.cos(2 * Math.PI * x / (N - 1))));
    }

    private void applyWindow(float[][] pixels, int picw, int pich) {
        for (int y = 0; y < pich; y++) {
            for (int x = 0; x < picw; x++) {
                pixels[x][y] *= hanning(x, picw) * hanning(y, pich);
            }
        }
    }

    private void log(float[][] pixels, int w, int h) {
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float v = pixels[x][y];
                pixels[x][y] = (float) Math.log(v);
            }
        }
    }

    private void clamp(float[][] pixels, int w, int h) {
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float v = pixels[x][y];
                pixels[x][y] = v >= 255 ? 255 : (v <= 0 ? 0 : v);
            }
        }
    }

    private float[][] copyMat(float[][] src, int w, int h) {
        float[][] dst = new float[w][h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                try {
                    dst[x][y] = src[x][y];
                } catch (ArrayIndexOutOfBoundsException e) {
                    dst[x][y] = 0;
                }
            }
        }
        return dst;
    }

    private float meanMat(float[][] mat, int w, int h) {
        float sum = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                sum += mat[x][y];
            }
        }
        return sum / (w * h);
    }

    private void analyze(Bitmap photo) {
        int picw = photo.getWidth();
        int pich = photo.getHeight();
        int len = Math.min(picw, pich);

        Log.i("debug", "width: " + picw + ", height: " + pich);

        float[][] pixels = toRGB(photo, len, len);
        applyWindow(pixels, len, len);
        float meanintensity = meanMat(pixels, len, len);

        float[][] mat = copyMat(pixels, len, len * 2);
        FloatFFT_2D fft = new FloatFFT_2D(len, len);
        fft.realForwardFull(mat);
        getMagnitude(mat, len, len);
        applyWindow(mat, len, len);
        float sum = meanMat(mat, len, len);

        BitmapDrawable fftbit = toBitmap(pixels, len, len);
        imageView.setImageDrawable(fftbit);

        float age = sum;
        ageView.setText("" + (int) age);

        //TODO: set `type`
        typeText.setText("당신의 피부타입은 " + typename[type] + "입니다.");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progress.isShowing())
                    progress.dismiss();
            }
        }, 500);
    }

}

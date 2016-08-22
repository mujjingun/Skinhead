package dimigo.org.skinhead;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.jtransforms.fft.FloatFFT_2D;

public class AnalyzeActivity extends AppCompatActivity {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze);

        imageView = (ImageView) findViewById(R.id.imageView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bitmap imageBitmap = (Bitmap) getIntent().getExtras().get("data");
        if (imageBitmap != null) {
            imageView.setImageBitmap(imageBitmap);
            analyze(imageBitmap);
        }
    }

    private float[][] toRGB(Bitmap bitmap) {
        int picw = bitmap.getWidth();
        int pich = bitmap.getHeight();
        int[] pix = new int[picw * pich];
        bitmap.getPixels(pix, 0, picw, 0, 0, picw, pich);

        float[][] map = new float[picw][pich * 2];

        for (int y = 0; y < pich; y++) {
            for (int x = 0; x < picw; x++) {
                int index = y * picw + x;
                int R = (pix[index] >> 16) & 0xff;     //bitwise shifting
                int G = (pix[index] >> 8) & 0xff;
                int B = pix[index] & 0xff;

                map[x][y] = (float) (R + G + B) / 255 / 3;
            }
        }
        return map;
    }

    private Bitmap toBitmap(float[][] pixels, int picw, int pich) {
        int[] ret = new int[picw * pich];

        for (int y = 0; y < pich; y++) {
            for (int x = 0; x < picw; x++) {
                int index = y * picw + x;
                int val = (int) (pixels[x][y] * 255);
                ret[index] = 0xff000000 | (val << 16) | (val << 8) | val;
            }
        }
        return Bitmap.createBitmap(ret, picw, pich, Bitmap.Config.ARGB_8888);
    }

    private void analyze(Bitmap photo) {
        int picw = photo.getWidth();
        int pich = photo.getHeight();
        float[][] pixels = toRGB(photo);
        FloatFFT_2D fft = new FloatFFT_2D(picw, pich);
        fft.realForwardFull(pixels);

        Bitmap fftbit = toBitmap(pixels, picw, pich);
        imageView.setImageBitmap(fftbit);

        TextView ageView = (TextView) findViewById(R.id.age);
        int age = (int) (pixels[50][50] * 100);
        ageView.setText("" + age);
    }

}

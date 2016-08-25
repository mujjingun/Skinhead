package dimigo.org.skinhead;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent it = getIntent();

        int type = it.getIntExtra("type", 0);
        int age = it.getIntExtra("age", 0);

    }
}

package dimigo.org.skinhead;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent it = getIntent();

        TextView tv = (TextView)findViewById(R.id.textView5);
        TextView tv1 = (TextView)findViewById(R.id.textView6);

        int type = it.getIntExtra("type", 0);
        if(type == 0) {
            tv.setText("지성피부");
            tv1.setText("당신의 피부는 피지의 분비량이 많은 피부입니다." +
                    "피부 트러블이 생길 확률이 높으니 철저한 세안을 통해 피부를 관리하세요.");
        } else if(type == 1) {
            tv.setText("복합성 피부");
            tv1.setText("당신의 피부는 T존에는 유분이 많지만 다른 부분에는 유분이 별로 없는 피부입니다." +
                    "불균형한 피지분비의 균형을 잡는 것이 중요합니다.");
        } else if(type == 2) {
            tv.setText("중성 피부");
            tv1.setText("중성 피부는 가장 이상적인 피부 타입입니다." +
                    "세안 직후에는 건조하지만 시간이 지나면 편안해 지고, 화장을 하지 않아도 피부가 거칠어 보이지 않습니다.");
        } else if(type == 3) {
            tv.setText("건성 피부");
            tv1.setText("피지의 분비가 적어 건조하고 윤기가 없는 피부입니다." +
                    "각질을 잘 제거하고, 수분크림을 통해 수분을 보충하세요.");
        }
    }
}

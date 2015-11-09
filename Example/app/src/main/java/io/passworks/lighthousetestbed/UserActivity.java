package io.passworks.lighthousetestbed;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import io.passworks.lighthouse.Lighthouse;
import io.passworks.lighthouse.model.User;

/**
 * Created by ivanbruel on 01/10/15.
 */
public class UserActivity extends AppCompatActivity {

    private TextView mIdentifiedAsTextView;
    private EditText mIdentifierEditText;
    private Button mIdentifyButton;
    private Button mLogoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mIdentifiedAsTextView = (TextView)findViewById(R.id.identifiedAsTextView);
        mIdentifierEditText = (EditText)findViewById(R.id.identifierEditText);
        mIdentifyButton = (Button)findViewById(R.id.identifyButton);
        mLogoutButton = (Button)findViewById(R.id.logoutButton);

        mIdentifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Lighthouse.getInstance().identifyUser(mIdentifierEditText.getText().toString());
                updateTextView();
            }
        });
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Lighthouse.getInstance().logoutUser();
                updateTextView();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        updateTextView();

    }

    private void updateTextView() {
        User user = Lighthouse.getInstance().getUser();
        if (user.isAnonymous()) {
            mIdentifiedAsTextView.setText("Identified as Anonymous\n<" + user.getId() + ">");
        } else {
            mIdentifiedAsTextView.setText("Identified as " + user.getId());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();

        }
        return super.onOptionsItemSelected(item);
    }
}

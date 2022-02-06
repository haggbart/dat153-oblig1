package com.haggbart.dat153.namequiz;

import static com.haggbart.dat153.namequiz.helper.ImageHelper.getUri;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.haggbart.dat153.namequiz.database.People;
import com.haggbart.dat153.namequiz.person.PersonEntry;

public class AddEntryActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AddEntryActivity";

    private static final People database = People.getInstance();
    private static final int REQUEST_IMAGE_GET = 1;

    private EditText forename;
    private EditText surname;
    private Uri imageUri;

    private ImageView ivImage;
    private ActivityResultLauncher<Intent> chooseImageResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);

        forename = findViewById(R.id.etForename);
        surname = findViewById(R.id.etSurname);

        ivImage = findViewById(R.id.ivImage);

        Button btnAddEntry = findViewById(R.id.btnAddEntry);
        Button btnChoose = findViewById(R.id.btnChoose);
        btnAddEntry.setOnClickListener(this);
        btnChoose.setOnClickListener(this);

        chooseImageResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null)
                        return;
                    imageUri = result.getData().getData();
                    getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Log.d(TAG, "onActivityResult: uri: " + imageUri);
                    Log.d(TAG, "onActivityResult: result: " + result);
                    ivImage.setImageURI(imageUri);
                }
        );
    }

    // TODO: refactor methods
    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick: button clicked: " + view.getResources().getResourceEntryName(view.getId()));

        if (view.getId() == R.id.btnChoose) {

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            chooseImageResult.launch(intent);
            return;
        }

        if (view.getId() != R.id.btnAddEntry) return;

        PersonEntry person = new PersonEntry(forename.getText().toString(), surname.getText().toString(), imageUri);

        Log.d(TAG, "onClick: imageUri: " + imageUri);

        if (imageUri == null || !database.add(person)) {
            Toast.makeText(this, "Please input all the required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        database.isDirty = true;
        ivImage.setImageURI(getUri(R.drawable.insert_photo));
        forename.setText("");
        surname.setText("");
        Toast.makeText(this, String.format("%s %s added to the database", person.getForename(), person.getSurname()), Toast.LENGTH_SHORT).show();
    }
}

package com.example.puzzle_game;

import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private GridView gridView;
    private ImageAdapter imageAdapter;
    private ArrayList<Bitmap> imageTiles = new ArrayList<>();
    private ArrayList<Bitmap> originalTiles = new ArrayList<>(); // Store correct order
    private int draggedIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridView = findViewById(R.id.gridView);
        loadAndSplitImage();
        Collections.shuffle(imageTiles);

        imageAdapter = new ImageAdapter();
        gridView.setAdapter(imageAdapter);

        gridView.setOnItemLongClickListener((parent, view, position, id) -> {
            draggedIndex = position;
            ClipData.Item item = new ClipData.Item(String.valueOf(position));
            ClipData dragData = new ClipData(
                    new ClipDescription("image_swap", new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}),
                    item);
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            view.startDragAndDrop(dragData, shadowBuilder, null, 0);
            return true;
        });

        gridView.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DROP:
                    int targetIndex = gridView.pointToPosition((int) event.getX(), (int) event.getY());
                    if (draggedIndex >= 0 && targetIndex >= 0 && targetIndex < imageTiles.size()) {
                        Collections.swap(imageTiles, draggedIndex, targetIndex);
                        imageAdapter.notifyDataSetChanged();
                        if (isPuzzleSolved()) {
                            Toast.makeText(MainActivity.this, "Puzzle Solved!", Toast.LENGTH_LONG).show();
                        }
                    }
                    draggedIndex = -1;
                    return true;
                default:
                    return true;
            }
        });
    }

    private void loadAndSplitImage() {
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.puzzle_image1);
        int tileSize = originalBitmap.getWidth() / 3;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Bitmap tile = Bitmap.createBitmap(originalBitmap, col * tileSize, row * tileSize, tileSize, tileSize);
                imageTiles.add(tile);
                originalTiles.add(tile); // Save original order
            }
        }
    }

    private boolean isPuzzleSolved() {
        for (int i = 0; i < imageTiles.size(); i++) {
            if (imageTiles.get(i) != originalTiles.get(i)) {
                return false;
            }
        }
        return true;
    }

    private class ImageAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return imageTiles.size();
        }

        @Override
        public Object getItem(int position) {
            return imageTiles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(MainActivity.this);
                imageView.setLayoutParams(new GridView.LayoutParams(250, 250));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageBitmap(imageTiles.get(position));
            return imageView;
        }
    }
}

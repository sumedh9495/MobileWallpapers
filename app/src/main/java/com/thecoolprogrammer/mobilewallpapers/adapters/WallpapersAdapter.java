package com.thecoolprogrammer.mobilewallpapers.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.thecoolprogrammer.mobilewallpapers.Manifest;
import com.thecoolprogrammer.mobilewallpapers.R;
import com.thecoolprogrammer.mobilewallpapers.models.Category;
import com.thecoolprogrammer.mobilewallpapers.models.Wallpaper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;

public class WallpapersAdapter extends RecyclerView.Adapter<WallpapersAdapter.WallpaperViewHolder> {

    private Context mCtx;
    private List<Wallpaper> wallpaperList;

    public WallpapersAdapter(Context context, List<Wallpaper> wallpaperList) {
        this.mCtx = context;
        this.wallpaperList = wallpaperList;
    }

    @NonNull
    @Override
    public WallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mCtx).inflate(R.layout.recyclerview_wallpapers , parent , false);
        return  new WallpaperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WallpaperViewHolder holder, int position) {
        Wallpaper w = wallpaperList.get(position);

        holder.textView.setText(w.title);
        Glide.with(mCtx)
                .load(w.url)
                .into(holder.imageView);

        if(w.isFavourite){
            holder.checkBoxFav.setChecked(true);
        }


    }

    @Override
    public int getItemCount() {
        return wallpaperList.size();
    }

    class WallpaperViewHolder extends  RecyclerView.ViewHolder
    implements  View.OnClickListener , CompoundButton.OnCheckedChangeListener{

        TextView textView;
        ImageView imageView;

        CheckBox checkBoxFav;
        ImageButton buttonShare , buttonDownload;

        public WallpaperViewHolder(View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.text_view_title);
            imageView = itemView.findViewById(R.id.image_view);

            checkBoxFav = itemView.findViewById(R.id.checkbox_favourite);
            buttonShare = itemView.findViewById(R.id.button_share);
            buttonDownload = itemView.findViewById(R.id.button_download);

            checkBoxFav.setOnCheckedChangeListener(this);
            buttonShare.setOnClickListener(this);
            buttonDownload.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            switch (v.getId()){

                case R.id.button_share:

                    shareWallpaper(wallpaperList.get(getAdapterPosition()));

                    break;

                case R.id.button_download:

                   downloadWallpaper(wallpaperList.get(getAdapterPosition()));

                    //getBitmapFromURL(wallpaperList.get(getAdapterPosition()).url);

                    break;

            }
        }


        public Bitmap getBitmapFromURL(String src){

            new Thread(){

                public void run(){



                }

            }.start();

            try {
                java.net.URL url = new java.net.URL(src);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        private  void shareWallpaper(Wallpaper w){

            ((Activity) mCtx).findViewById(R.id.progressbar).setVisibility(View.VISIBLE);

            Glide.with(mCtx)
                    .asBitmap()
                    .load(w.url)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {

                            ((Activity) mCtx).findViewById(R.id.progressbar).setVisibility(View.GONE);


                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("image/*");
                            intent.putExtra(Intent.EXTRA_STREAM , getLocalBitmapUri(resource));

                            mCtx.startActivity(Intent.createChooser(intent,"Mobile Wallpapers"));


                        }
                    });

        }

        private Uri getLocalBitmapUri(Bitmap bmp){

            Uri bmpUri = null;


            try {
                File file = new File(mCtx.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "mobile_wallpaper"+System.currentTimeMillis()+".png");


                FileOutputStream out  =  new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG,90,out);

                out.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return bmpUri;
        }


        private void downloadWallpaper(final Wallpaper wallpaper){


            ((Activity) mCtx).findViewById(R.id.progressbar).setVisibility(View.VISIBLE);

            Glide.with(mCtx)
                    .asBitmap()
                    .load(wallpaper.url)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            ((Activity) mCtx).findViewById(R.id.progressbar).setVisibility(View.GONE);

                            Intent intent = new Intent(Intent.ACTION_VIEW);

                            Uri uri = saveWallpaperAndGetUri(resource , wallpaper.id );

                            if(uri != null){
                                intent.setDataAndType(uri , "image/*");
                                mCtx.startActivity(Intent.createChooser(intent,"Mobile Wallpapers"));

                            }



                        }
                    });

        }

        private  Uri saveWallpaperAndGetUri(Bitmap bitmap , String id){
            if(ContextCompat.checkSelfPermission(mCtx , android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    ){

                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mCtx,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){


                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);

                    Uri uri = Uri.fromParts("package",mCtx.getPackageName(),null);
                    intent.setData(uri);

                    mCtx.startActivity(intent);

                }else {
                    ActivityCompat.requestPermissions((Activity) mCtx,new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},100);
                }

                return null;

            }

            File folder = new File(Environment.getExternalStorageDirectory().toString() + "/mobile_wallpapers");
            folder.mkdirs();

            File file = new File(folder , id+".jpg");

            try {
                FileOutputStream out =  new FileOutputStream(file);

                bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);
                out.flush();
                out.close();

                return  Uri.fromFile(file);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return  null;
        }


        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            if(FirebaseAuth.getInstance().getCurrentUser() == null){
                Toast.makeText(mCtx , "Please login first",Toast.LENGTH_LONG).show();
                buttonView.setChecked(false);
                return;
            }


            int position = getAdapterPosition();
            Wallpaper w = wallpaperList.get(position);


            DatabaseReference dbFavs = FirebaseDatabase.getInstance().getReference("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("favourites")
                    .child(w.category);



            if(isChecked){
                dbFavs.child(w.id).setValue(w);

            }else {

                dbFavs.child(w.id).setValue(null);
            }

        }
    }

}

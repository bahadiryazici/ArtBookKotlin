package com.example.artbookkotlin
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.artbookkotlin.databinding.ActivityDetailsBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream

class DetailsActivity : AppCompatActivity() {


    private lateinit var binding: ActivityDetailsBinding
    private lateinit var activitylauncher : ActivityResultLauncher<Intent>
    private lateinit var permissionlauncher : ActivityResultLauncher<String>
    private lateinit var database : SQLiteDatabase
    var selectedBitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        registerlauncher()

        val intent = intent
        val info = intent.getIntExtra("info",0)
        if (info == 0){

            binding.artName.setText("")
            binding.artistName.setText("")
            binding.artYear.setText("")
            binding.button.visibility = View.VISIBLE
            binding.imageView.setImageResource(R.drawable.ic_launcher_background)
        }else{

            binding.button.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id",1)

            try{

                database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)

                val cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedId.toString()))

                val artnameIx = cursor.getColumnIndex("artname")
                val artistNameIx = cursor.getColumnIndex("artistname")
                val artYearIx = cursor.getColumnIndex("artyear")
                val imageIx = cursor.getColumnIndex("image")

                while (cursor.moveToNext()){

                    binding.artName.setText(cursor.getString(artnameIx))
                    binding.artistName.setText(cursor.getString(artistNameIx))
                    binding.artYear.setText(cursor.getString(artYearIx))


                    val byteArray = cursor.getBlob(imageIx)
                    val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                    binding.imageView.setImageBitmap(bitmap)

                }
                cursor.close()


            }catch (e : Exception){
                e.printStackTrace()
            }


        }
    }




    fun selectImage(view:View){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            //android33 read_external_Images
            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_MEDIA_IMAGES) ){
                    Snackbar.make(view,"Permission Needed",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission"){
                        //ask permission
                        permissionlauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                    }.show()

                }else{
                    //ask permission
                    permissionlauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                }
            }else{
                //Intent go gallery
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activitylauncher.launch(intentToGallery)
            }
        }else{
            //android33< read_external_Storage
            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) ){
                    Snackbar.make(view,"Permission Needed",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission"){
                        //ask permission
                        permissionlauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }.show()

                }else{
                    //ask permission
                    permissionlauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }else{
                //Intent go gallery
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activitylauncher.launch(intentToGallery)
            }
        }



    }

    fun registerlauncher(){

        activitylauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->

            if(result.resultCode == RESULT_OK){
                val intentFromresult = result.data
                if(intentFromresult != null) {
                    val imageData = intentFromresult.data
                    if (imageData != null){
                        try {
                            if(Build.VERSION.SDK_INT>=28){
                                val source = ImageDecoder.createSource(this.contentResolver, imageData)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)

                            }else{

                                selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver,imageData)
                                binding.imageView.setImageBitmap(selectedBitmap)


                            }


                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

        }

        permissionlauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->

            if(result){

                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activitylauncher.launch(intentToGallery)

            }else{
                Toast.makeText(this,"Permission needed!",Toast.LENGTH_LONG).show()
            }
        }
    }

    fun save(view:View){

        val artName = binding.artName.text.toString()
        val artistName = binding.artistName.text.toString()
        val artYear = binding.artYear.text.toString()


        if(selectedBitmap != null){
            val smallBitmap = makeSmallerBitMap(selectedBitmap!!,300)

            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,80,outputStream)
            val byteArray = outputStream.toByteArray()

            try {

                database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)

                database.execSQL("CREATE TABLE IF NOT EXISTS arts(id INTEGER PRIMARY KEY, artname VARCHAR, artistname VARCHAR, artyear VARACHAR, image BLOB)")

                val sqlString = "INSERT INTO arts(artname,artistname,artyear,image) VALUES(?,?,?,?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1,artName)
                statement.bindString(2,artistName)
                statement.bindString(3,artYear)
                statement.bindBlob(4,byteArray)
                statement.execute()

            }catch (e:java.lang.Exception){
                e.printStackTrace()
            }

            val intent = Intent(DetailsActivity@this,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)

        }



    }

    private fun makeSmallerBitMap(image: Bitmap, maximumSize:Int):Bitmap{

        var width = image.width
        var height = image.height

        val scaledRatio : Double = width.toDouble() / height.toDouble()

        if(scaledRatio > 1){
            width = maximumSize
            val scaledHeight = (width / scaledRatio).toInt()
            height = scaledHeight

        }else{
            height = maximumSize
            val scaledWidth = (height * scaledRatio).toInt()
            width = scaledWidth
        }


        return Bitmap.createScaledBitmap(image,width,height,true)
    }
}
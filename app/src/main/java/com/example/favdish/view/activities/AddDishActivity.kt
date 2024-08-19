package com.example.favdish.view.activities

import android.Manifest
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.favdish.App
import com.example.favdish.R
import com.example.favdish.databinding.ActivityAddDishBinding
import com.example.favdish.databinding.DialogCustomListBinding
import com.example.favdish.databinding.DialogCustomSelectionBinding
import com.example.favdish.model.entities.FavDish
import com.example.favdish.utils.Constants
import com.example.favdish.view.adapters.ListItemAdapter
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory
import timber.log.Timber
import java.io.IOException

@Suppress("DEPRECATED_IDENTITY_EQUALS")
class AddDishActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var addDishBinding: ActivityAddDishBinding
    private lateinit var addCustomListDialog: Dialog
    private var dishDetails: FavDish? = null
    private lateinit var imageUri : Uri
    private var imageStoragePath: String = ""
    private val favDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((application as App).repository)
    }


    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        permissions.entries.forEach {
            val isGranted = it.value
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions denied. You need to allow permissions to use this feature",
                    Toast.LENGTH_SHORT
                ).show()
                Timber.tag("Permissions").d("Permissions denied.")
            }
        }
    }
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {result ->
            val image = addDishBinding.ivDishImage
            if (result.resultCode === RESULT_OK) {
                val inputImage = uriToBitmap(imageUri)
                image.setImageBitmap(inputImage)
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
            val image = addDishBinding.ivDishImage
            if (result.resultCode === RESULT_OK) {
                val imageUri = result.data!!.data
                image.setImageURI(imageUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addDishBinding = ActivityAddDishBinding.inflate(layoutInflater)
        setContentView(addDishBinding.root)
        setUpActionBar()

        addDishBinding.ivAddDishImage.setOnClickListener(this@AddDishActivity)
        addDishBinding.etType.setOnClickListener(this@AddDishActivity)
        addDishBinding.etCategory.setOnClickListener(this@AddDishActivity)
        addDishBinding.etCookingTime.setOnClickListener(this@AddDishActivity)
        addDishBinding.btnAddDish.setOnClickListener(this@AddDishActivity)
    }

    override fun onClick(view: View) {
            when (view.id) {
                addDishBinding.ivAddDishImage.id -> {
                    customSelectionDialog()
                    return
                }

                addDishBinding.etType.id -> {
                    customItemsListDialog(
                        resources.getString(R.string.title_select_dish_type),
                        Constants.dishTypes(),
                        Constants.DISH_TYPE
                    )
                    return
                }

                addDishBinding.etCategory.id -> {
                    customItemsListDialog(
                        resources.getString(R.string.title_select_dish_category),
                        Constants.dishCategories(),
                        Constants.DISH_CATEGORY
                    )
                    return
                }

                addDishBinding.etCookingTime.id -> {
                    customItemsListDialog(
                        resources.getString(R.string.title_select_dish_cooking_time),
                        Constants.dishCookTime(),
                        Constants.DISH_COOKING_TIME
                    )
                    return
                }

                addDishBinding.btnAddDish.id -> {
                    val title = addDishBinding.etTitle.text.toString().trim { it <= ' ' }
                    val type = addDishBinding.etType.text.toString().trim { it <= ' ' }
                    val category = addDishBinding.etCategory.text.toString().trim { it <= ' ' }
                    val ingredients =
                        addDishBinding.etIngredients.text.toString().trim { it <= ' ' }
                    val cookingTimeInMinutes =
                        addDishBinding.etCookingTime.text.toString().trim { it <= ' ' }
                    val cookingDirection =
                        addDishBinding.etDirectionToCook.text.toString().trim { it <= ' ' }

                    when {
                        TextUtils.isEmpty(imageStoragePath) -> {
                            Toast.makeText(
                                this@AddDishActivity,
                                resources.getString(R.string.err_msg_select_dish_image),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(title) -> {
                            Toast.makeText(
                                this@AddDishActivity,
                                resources.getString(R.string.err_msg_enter_dish_title),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(type) -> {
                            Toast.makeText(
                                this@AddDishActivity,
                                resources.getString(R.string.err_msg_select_dish_type),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(category) -> {
                            Toast.makeText(
                                this@AddDishActivity,
                                resources.getString(R.string.err_msg_select_dish_category),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(ingredients) -> {
                            Toast.makeText(
                                this@AddDishActivity,
                                resources.getString(R.string.err_msg_enter_dish_ingredients),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(cookingTimeInMinutes) -> {
                            Toast.makeText(
                                this@AddDishActivity,
                                resources.getString(R.string.err_msg_select_dish_cooking_time),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(cookingDirection) -> {
                            Toast.makeText(
                                this@AddDishActivity,
                                resources.getString(R.string.err_msg_enter_dish_cooking_instructions),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> {
                            val favDishDetails = FavDish(
                                imageStoragePath,
                                Constants.DISH_IMAGE_SOURCE_LOCAL,
                                title, type, category, ingredients, cookingTimeInMinutes,
                                cookingDirection, false
                            )
                            favDishViewModel.insert(favDishDetails)
                            finish()
                        }
                    }
                }
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(addDishBinding.tbrAddDish)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        addDishBinding.tbrAddDish.setNavigationOnClickListener{onBackPressedDispatcher.onBackPressed()}
    }

    private fun customSelectionDialog() {
        val dialog = Dialog(this@AddDishActivity)

        val binding: DialogCustomSelectionBinding = DialogCustomSelectionBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        dialog.show()

        binding.tvCamera.setOnClickListener {
            val cameraPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED -> { startCamera() }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.CAMERA) -> { showRationalDialog() }
                else -> {
                    cameraPermissionLauncher.launch(cameraPermissions)
                }
            }
        }

        binding.tvGallery.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(galleryIntent)
        }
    }

    private fun startCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(cameraIntent)
    }

    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun showRationalDialog() {
        AlertDialog.Builder(this)
            .setMessage("It Looks like you have turned off permissions required for this feature. It can be enabled in Settings")
            .setPositiveButton(
                "GO TO SETTINGS"
            ) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) { e.printStackTrace() }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }.show()
    }

    //done
    private fun customItemsListDialog(title: String, itemsList: List<String>, selection: String) {
        addCustomListDialog = Dialog(this@AddDishActivity)

        val binding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)
        addCustomListDialog.setContentView(binding.root)

        binding.tvDialogTitle.text = title
        binding.rvDialogList.layoutManager = LinearLayoutManager(this@AddDishActivity)
        val adapter = ListItemAdapter(this@AddDishActivity, null, itemsList, selection)
        binding.rvDialogList.adapter = adapter

        addCustomListDialog.show()
    }

    //done
    fun selectedListItem(item: String, selection: String) {
        when (selection) {
            Constants.DISH_TYPE -> {
                addCustomListDialog.dismiss()
                addDishBinding.etType.setText(item)
            }

            Constants.DISH_CATEGORY -> {
                addCustomListDialog.dismiss()
                addDishBinding.etCategory.setText(item)
            }
            else -> {
                addCustomListDialog.dismiss()
                addDishBinding.etCookingTime.setText(item)
            }
        }
    }
}
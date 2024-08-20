package com.example.favdish.view.activities

import android.Manifest
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.favdish.App
import com.example.favdish.R
import com.example.favdish.base.BaseActivity
import com.example.favdish.databinding.ActivityAddEditDishBinding
import com.example.favdish.databinding.DialogCustomListBinding
import com.example.favdish.databinding.DialogCustomSelectionBinding
import com.example.favdish.model.entities.FavDish
import com.example.favdish.utils.Constants
import com.example.favdish.view.adapters.ListItemAdapter
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

@Suppress("DEPRECATED_IDENTITY_EQUALS")
class AddEditDishActivity : View.OnClickListener, BaseActivity<ActivityAddEditDishBinding>(
    ActivityAddEditDishBinding::inflate
) {
    private lateinit var addCustomListDialog: Dialog
    private var dishDetails: FavDish? = null
    private lateinit var imageUri : Uri
    private var imageStoragePath: String = ""
    private val favDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((application as App).repository)
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (!permissions.containsValue(false)) {
                startCamera()
            } else {
                Toast.makeText(this, "Unable to use this feature, please allow permission(s).", Toast.LENGTH_SHORT).show()
                Timber.tag("Permissions").d("Permissions denied.")
        }
    }

    @Suppress("DEPRECATION")
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {result ->
            if (result.resultCode === RESULT_OK) {
                val inputImage = MediaStore.Images.Media.getBitmap(contentResolver,imageUri)
                Glide.with(this@AddEditDishActivity)
                    .load(inputImage)
                    .centerCrop()
                    .into(binding.ivDishImage)

                imageStoragePath = saveImageToInternalStorage(inputImage!!)

                binding.ivAddDishImage.setImageDrawable(
                    ContextCompat.getDrawable(this@AddEditDishActivity, R.drawable.ic_edit)
                )
            }

    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
            val image = binding.ivDishImage
            if (result.resultCode === RESULT_OK) {
                val imageUri = result.data!!.data
                image.setImageURI(imageUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpActionBar()

        binding.ivAddDishImage.setOnClickListener(this@AddEditDishActivity)
        binding.etType.setOnClickListener(this@AddEditDishActivity)
        binding.etCategory.setOnClickListener(this@AddEditDishActivity)
        binding.etCookingTime.setOnClickListener(this@AddEditDishActivity)
        binding.btnAddDish.setOnClickListener(this@AddEditDishActivity)
    }

    override fun onClick(view: View) {
            when (view.id) {
                binding.ivAddDishImage.id -> {
                    customSelectionDialog()
                    return
                }

                binding.etType.id -> {
                    customItemsListDialog(
                        resources.getString(R.string.title_select_dish_type),
                        Constants.dishTypes(),
                        Constants.DISH_TYPE
                    )
                    return
                }

                binding.etCategory.id -> {
                    customItemsListDialog(
                        resources.getString(R.string.title_select_dish_category),
                        Constants.dishCategories(),
                        Constants.DISH_CATEGORY
                    )
                    return
                }

                binding.etCookingTime.id -> {
                    customItemsListDialog(
                        resources.getString(R.string.title_select_dish_cooking_time),
                        Constants.dishCookTime(),
                        Constants.DISH_COOKING_TIME
                    )
                    return
                }

                binding.btnAddDish.id -> {
                    val title = binding.etTitle.text.toString().trim { it <= ' ' }
                    val type = binding.etType.text.toString().trim { it <= ' ' }
                    val category = binding.etCategory.text.toString().trim { it <= ' ' }
                    val ingredients =
                        binding.etIngredients.text.toString().trim { it <= ' ' }
                    val cookingTimeInMinutes =
                        binding.etCookingTime.text.toString().trim { it <= ' ' }
                    val cookingDirection =
                        binding.etDirectionToCook.text.toString().trim { it <= ' ' }

                    when {
                        TextUtils.isEmpty(imageStoragePath) -> {
                            Toast.makeText(
                                this@AddEditDishActivity,
                                resources.getString(R.string.err_msg_select_dish_image),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(title) -> {
                            Toast.makeText(
                                this@AddEditDishActivity,
                                resources.getString(R.string.err_msg_enter_dish_title),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(type) -> {
                            Toast.makeText(
                                this@AddEditDishActivity,
                                resources.getString(R.string.err_msg_select_dish_type),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(category) -> {
                            Toast.makeText(
                                this@AddEditDishActivity,
                                resources.getString(R.string.err_msg_select_dish_category),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(ingredients) -> {
                            Toast.makeText(
                                this@AddEditDishActivity,
                                resources.getString(R.string.err_msg_enter_dish_ingredients),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(cookingTimeInMinutes) -> {
                            Toast.makeText(
                                this@AddEditDishActivity,
                                resources.getString(R.string.err_msg_select_dish_cooking_time),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(cookingDirection) -> {
                            Toast.makeText(
                                this@AddEditDishActivity,
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
                            Toast.makeText(this,"Added successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.tbrAddDish)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        binding.tbrAddDish.setNavigationOnClickListener{onBackPressedDispatcher.onBackPressed()}
    }

    private fun customSelectionDialog() {
        val dialog = Dialog(this@AddEditDishActivity)

        val binding: DialogCustomSelectionBinding = DialogCustomSelectionBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        dialog.show()

        binding.tvCamera.setOnClickListener {
            val cameraPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
            permissionLauncher.launch(cameraPermissions)
            dialog.dismiss()
        }

        binding.tvGallery.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(galleryIntent)
            dialog.dismiss()
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

    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val wrapper = ContextWrapper(applicationContext)

        var file = wrapper.getDir("FavDishImages", Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) { e.printStackTrace() }
        return file.absolutePath
    }

    //done
    private fun customItemsListDialog(title: String, itemsList: List<String>, selection: String) {
        addCustomListDialog = Dialog(this@AddEditDishActivity)

        val binding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)
        addCustomListDialog.setContentView(binding.root)

        binding.tvDialogTitle.text = title
        binding.rvDialogList.layoutManager = LinearLayoutManager(this@AddEditDishActivity)
        val adapter = ListItemAdapter(this@AddEditDishActivity, null, itemsList, selection)
        binding.rvDialogList.adapter = adapter

        addCustomListDialog.show()
    }

    //done
    fun selectedListItem(item: String, selection: String) {
        when (selection) {
            Constants.DISH_TYPE -> {
                addCustomListDialog.dismiss()
                binding.etType.setText(item)
            }

            Constants.DISH_CATEGORY -> {
                addCustomListDialog.dismiss()
                binding.etCategory.setText(item)
            }
            else -> {
                addCustomListDialog.dismiss()
                binding.etCookingTime.setText(item)
            }
        }
    }
}
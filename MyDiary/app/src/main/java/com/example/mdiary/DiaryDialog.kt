package com.example.mdiary

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.transition.Visibility
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.example.mdiary.data.DiaryItem
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.diary_item_dialog.view.*
import pl.aprilapps.easyphotopicker.*
import java.util.*


class DiaryDialog : DialogFragment() {

    interface DiaryHandler {
        fun diaryItemCreated(diaryItem: DiaryItem)
    }


    lateinit var diaryHandler: DiaryHandler

    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null

    private var longitude: Double? = null
    private var latitude: Double? = null

    lateinit var easyImage: EasyImage
    private var photo: MediaFile? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is DiaryHandler) {
            diaryHandler = context
        } else {
            throw RuntimeException(
                "The Activity does not implement the DiaryHandler interface!"
            )
        }
    }

    lateinit var etDiaryItemTitle: EditText
    lateinit var etDiaryItemDescription: EditText
    lateinit var etDiaryItemCreatePlace: EditText
    lateinit var etDiaryItemCreateDate: EditText
    lateinit var btnSetMyPos: Button
    lateinit var cbIsPersonal: CheckBox
    lateinit var btnChooser: Button
    lateinit var ivPhoto: ImageView

    lateinit var tvPos: TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(requireContext())

        dialogBuilder.setTitle("Add diary item")
        val dialogView = requireActivity().layoutInflater.inflate(
            R.layout.diary_item_dialog, null
        )
        etDiaryItemTitle = dialogView.etDiaryItemTitle
        etDiaryItemDescription = dialogView.etDiaryItemDescription
        etDiaryItemCreatePlace = dialogView.etDiaryItemCreatePlace
        etDiaryItemCreateDate = dialogView.etDiaryItemCreateDate
        cbIsPersonal = dialogView.cbDiaryItemIsPersonal
        btnSetMyPos = dialogView.btnSetMyPos
        btnChooser = dialogView.btnChooser
        ivPhoto = dialogView.ivPhoto
        tvPos = dialogView.tvPos

        dialogBuilder.setView(dialogView)

        dialogBuilder.setNegativeButton("Cancel") {
                dialog, which ->
        }
        dialogBuilder.setPositiveButton("Add") {
                dialog, which ->

        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        locationRequest = LocationRequest.create();
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
        locationRequest?.interval = 20 * 1000;
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location: Location in locationResult.locations) {
                    latitude = location.latitude;
                    longitude = location.longitude;
                }
            }
        }

        btnSetMyPos.setOnClickListener{view ->
            checkLocationPermission()
        }

        easyImage = EasyImage.Builder(requireContext())
            .setChooserTitle("Pick media")
            .setCopyImagesToPublicGalleryFolder(false)
            .setChooserType(ChooserType.CAMERA_AND_GALLERY)
            .setFolderName("EasyImage sample")
            .allowMultiple(true)
            .build()

        btnChooser.setOnClickListener{ view ->
            val necessaryPermissions = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (arePermissionsGranted(necessaryPermissions)) {
                easyImage.openChooser(this)
            } else {
                requestPermissionsCompat(necessaryPermissions, CHOOSER_PERMISSIONS_REQUEST_CODE)
            }
        }

        return dialogBuilder.create()
    }


    override fun onResume() {
        super.onResume()

       initDatePickerDialog()

        val positiveButton = (dialog as AlertDialog).getButton(Dialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            if (etDiaryItemTitle.text.isNotEmpty() && etDiaryItemDescription.text.isNotEmpty()) {
                handleDiaryItemCreate()
                (dialog as AlertDialog).dismiss()
            } else {
                if(etDiaryItemTitle.text.isEmpty())
                    etDiaryItemTitle.error = "This field can not be empty"
                else if(etDiaryItemDescription.text.isEmpty())
                    etDiaryItemDescription.error = "This field can not be empty"
            }
        }
    }

    private fun initDatePickerDialog(){
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        etDiaryItemCreateDate.setOnClickListener {
            val dpd = DatePickerDialog(requireContext(), DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                etDiaryItemCreateDate.setText("$dayOfMonth.$monthOfYear.$year")
            }, year, month, day)
            dpd.show();
        }
    }

    private fun handleDiaryItemCreate() {
        diaryHandler.diaryItemCreated(
            DiaryItem(
                null,
                etDiaryItemTitle.text.toString(),
                etDiaryItemDescription.text.toString(),
                etDiaryItemCreateDate.text.toString(),
                etDiaryItemCreatePlace.text.toString(),
                longitude,
                latitude,
                cbIsPersonal.isChecked,
                photo?.file?.absolutePath
            )
        )
    }

    fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(context)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton("OK") { _, _ ->
                        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_LOCATION)
                    }
                    .create()
                    .show()
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_LOCATION)
            }
        }
        else if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            getLastLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        getLastLocation()
                    }
                } else {
                    Toast.makeText(context, "Permission denied!", Toast.LENGTH_LONG).show()
                }
                return
            }
            CHOOSER_PERMISSIONS_REQUEST_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    easyImage.openChooser(this);
                }
                return
            }
            CAMERA_REQUEST_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    easyImage.openCameraForImage(this);
                }
                return
            }
            GALLERY_REQUEST_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    easyImage.openGallery(this);
                }
                return
            }
        }
    }

    private fun getLastLocation(){
        mFusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                if(location != null) {
                    longitude = location?.longitude
                    latitude = location?.latitude
                    tvPos.text = ("" + longitude + ":" + latitude)
                }
                else {
                    mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                }
            }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        easyImage.handleActivityResult(
            requestCode,
            resultCode,
            data,
            requireActivity(),
            object : DefaultCallback() {
                override fun onMediaFilesPicked(
                    imageFiles: Array<MediaFile>,
                    source: MediaSource
                ) {
                    onPhotosReturned(imageFiles)
                }

                override fun onImagePickerError(
                    error: Throwable,
                    source: MediaSource
                ) {
                    error.printStackTrace()
                }

                override fun onCanceled(source: MediaSource) {
                    //Not necessary to remove any files manually anymore
                }
            })
    }

    private fun onPhotosReturned(returnedPhotos: Array<MediaFile>) {
        if(returnedPhotos.isNotEmpty()){
            photo = returnedPhotos[0]
            Log.d("EasyImage", "Image file returned: ${photo!!.file.absolutePath}")
            val bitmap = BitmapFactory.decodeFile(photo!!.file.absolutePath)
            ivPhoto.setImageBitmap(bitmap)
            ivPhoto.visibility = View.VISIBLE
        }
    }

    private fun arePermissionsGranted(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) return false
        }
        return true
    }

    private fun requestPermissionsCompat(
        permissions: Array<String>,
        requestCode: Int
    ) {
        requestPermissions(permissions, requestCode)
    }

    companion object {
        val MY_PERMISSIONS_REQUEST_LOCATION = 99
        val CHOOSER_PERMISSIONS_REQUEST_CODE = 7459
        val CAMERA_REQUEST_CODE = 7500
        val GALLERY_REQUEST_CODE = 7502
    }

}
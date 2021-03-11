package com.example.facedetector

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class MainActivity : AppCompatActivity() {
    private lateinit var mImageURI: Uri
    lateinit var currentPhotoPath: String

    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 1)
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 5)
        }







        val imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.teste1)

        findViewById<ImageView>(R.id.normal).setImageBitmap(imageBitmap)




        calculateInfo(imageBitmap)


    }

    private fun calculateInfo(bitmap: Bitmap){


        val metrics = resources.displayMetrics

        val faceConfig = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                .build()




        val imageInputImage =  InputImage.fromBitmap(bitmap, 0)
        val bmp: Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        val detector = FaceDetection.getClient(faceConfig)

        val canvas = Canvas(bmp)

        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = Color.YELLOW
        paint.isAntiAlias = true

        val rect = Rect()
        var cont = 0

        val result = detector.process(imageInputImage)
                .addOnSuccessListener { faces ->
                    faces?.let {
                        if(it.size > 0){
                            for (faceContour in it[0].allContours) {

                                if(faceContour.faceContourType == FaceContour.NOSE_BRIDGE){
                                    Log.d("xxx", "cont: $cont")
                                } else {
                                    cont++
                                }

                                for(point in faceContour.points) {

                                    val xInitial = point.x.toInt()
                                    val yInitial = point.y.toInt()

                                    rect.left = xInitial
                                    rect.top = yInitial
                                    rect.bottom = yInitial + 50
                                    rect.right = xInitial + 50

                                    canvas.drawRect(rect, paint)
                                }
                            }

                            val pontosDaFace = faces[0].allContours[11].points
                            val bottom = pontosDaFace[1]
                            val top = pontosDaFace[0]

                            val rect1 = Rect()
                            rect1.left = bottom.x.toInt()
                            rect1.top = bottom.y.toInt()
                            rect1.bottom = bottom.y.toInt() + 50
                            rect1.right = bottom.x.toInt() + 50

                            val rect2 = Rect()
                            rect2.left = top.x.toInt()
                            rect2.top = top.y.toInt()
                            rect2.bottom = top.y.toInt() + 50
                            rect2.right = top.x.toInt() + 50

                            val paint1 = Paint()
                            paint1.color = Color.RED

                            val paint2 = Paint()
                            paint1.color = Color.GREEN

                            canvas.drawRect(rect1, paint1)
                            canvas.drawRect(rect2, paint2)

                            val noise = calculateMeasurementCInPixel(pontosDaFace[0], pontosDaFace[1])
                            val noiseToChin = calculateDistanceBeteweenTwoPoints(pontosDaFace[1], faces[0].allContours[0].points[18])
                            val totalSize = calculateMeasurementAInPixel(pontosDaFace[0], pontosDaFace[1], faces[0].allContours[0].points[18])

                            val totalSizeWidth = calculateDistanceBeteweenTwoPoints(faces[0].allContours[0].points[28], faces[0].allContours[0].points[8]).first


                            Log.d("xxx", "Largura: ${bitmap.width}")
                            Log.d("xxx", "Altura: ${bitmap.height}")
                            Log.d("xxx", "Pixel Nariz ao Queixo: $totalSize")
                            Log.d("xxx", "Altura: ${faces[0].allContours[0].points[18].y - pontosDaFace[0].y}")

                            val pixelPerCm = bitmap.height / 30
                            val distanciaEuclidiana = 0

                            Log.d("xxx", "pixelPerCM: $pixelPerCm")
                            val rectSuperior = Rect()
                            rectSuperior.left = (bitmap.width * 0.8).toInt()
                            rectSuperior.top = pixelPerCm * 2
                            rectSuperior.bottom = pixelPerCm * 2 + 50
                            rectSuperior.right = (bitmap.width * 0.8).toInt() + 50

                            canvas.drawRect(rectSuperior,  paint2)

                            Log.d("xxx", "Tamanho do Nariz até o Queixo: ${totalSize / pixelPerCm}")
                            Log.d("xxx", "Tamanho do rosto: ${totalSizeWidth / pixelPerCm}")


                            Toast.makeText(this, totalSize.toString(), Toast.LENGTH_LONG).show()

                            Log.d("aaa", "Teste de altura: $totalSize")
                            Log.d("xxx", "Em Cm: ")
                            //Log.d("xxx", "Teste de altura: ${calculateMeasurementCInPixel(pontosDaFace[0], pontosDaFace[1])}")
                            Log.d("xxx", "density: ${metrics.scaledDensity}")


                            findViewById<ImageView>(R.id.contorno).setImageBitmap(bmp)

                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.d("xxx", "onCreate: Fail to process image - ${e.message}")
                }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == 2 && resultCode == RESULT_OK) {
            //val extras = data?.extras

            contentResolver.notifyChange(mImageURI,  null)
            val cr = contentResolver

            val bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageURI)
            findViewById<ImageView>(R.id.normal).setImageBitmap(bitmap)


            //val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, data?.data)

            //val bitmap: Bitmap = extras?.get("data") as Bitmap

            Log.d("aaa", "onActivityResult: ${bitmap.density}")
            Log.d("aaa", "onActivityResult: ${bitmap.height}")
            Log.d("aaa", "onActivityResult: ${bitmap.width}")


            calculateInfo(bitmap)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun calculateMeasurementAInPixel(topNoisePoint: PointF, bottomNoisePoint: PointF, bottomNoiseChin: PointF): Float {
        val noiseMeasurement = (calculateDistanceBeteweenTwoPoints(topNoisePoint, bottomNoisePoint).second).toFloat()
        val noiseAtChinMeasurement = calculateDistanceBeteweenTwoPoints(bottomNoisePoint, bottomNoiseChin).second
        return noiseMeasurement + noiseAtChinMeasurement
    }

    private fun calculateMeasurementBInPixel(leftFacePoint: PointF, rightFacePoint: PointF): Float {
        return calculateDistanceBeteweenTwoPoints(leftFacePoint, rightFacePoint).first
    }

    private fun calculateMeasurementCInPixel(pointA: PointF, pointB: PointF): Float {
        return calculateDistanceBeteweenTwoPoints(pointA, pointB).second
    }

    private fun calculateDistanceBeteweenTwoPoints(moreLowerPoint: PointF, moreHigherPoint: PointF): Pair<Float, Float> {
        return Pair(moreHigherPoint.x - moreLowerPoint.x, moreHigherPoint.y - moreLowerPoint.y)
    }
}
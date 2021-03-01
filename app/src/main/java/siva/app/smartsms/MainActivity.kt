package siva.app.smartsms

import android.Manifest
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import siva.app.smartsms.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    var permission = false
    private val myPermissionsRequestReadSms = 77
    private val myPermissionsRequestReceiveSms = 10
    lateinit var smsReceiver: SMSReceiver

    private var smsList: ArrayList<SMS_Item>? = null
    var smsListDatemap: HashMap<Long, ArrayList<SMS_Item>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = checkSmsPermission()
            if (permission) {
                initSMSList()
            }
        }

        initSMSReceiver()
    }

    private fun initSMSReceiver() {
        smsReceiver = SMSReceiver()
        val smsFilter = IntentFilter()
        smsFilter.addAction("android.provider.Telephony.SMS_RECEIVED")
        registerReceiver(smsReceiver, smsFilter)
    }

    override fun onDestroy() {
        unregisterReceiver(smsReceiver)
        super.onDestroy()
    }

    private fun initSMSList() {
        smsList = ArrayList()
        var listItem: SMS_Item
        val uriSms: Uri = Uri.parse("content://sms/inbox")
        val cursor: Cursor? = contentResolver.query(
            uriSms, arrayOf(
                "_id",
                "address",
                "date",
                "body"
            ), null, null, "date DESC"
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val id: String = cursor.getString(0)
                val address: String = cursor.getString(1)
                val msg: String = cursor.getString(3)
                val date: String = cursor.getString(2)
                listItem = SMS_Item(id, date, msg, address, 0)
                smsList!!.add(listItem)
            }
        }
        filterSMSList(smsList!!)
        val filteredSMSList: ArrayList<SMS_Item> = ArrayList()
        val headersList: ArrayList<Long> = ArrayList()

        smsListDatemap!!.keys.toSortedSet().forEach {

            if (it in 0..48) {
                if (it in 0..2) {
                    if (!headersList.contains(2)) {
                        filteredSMSList.add(SMS_Item("", "$it hours ago", "", "", 1))
                    }
                    headersList.add(2)
                } else if (it in 3..6) {
                    if (!headersList.contains(3)) {
                        filteredSMSList.add(SMS_Item("", "3 hours ago", "", "", 1))
                    }
                    headersList.add(3)
                } else if (it in 6..12) {
                    if (!headersList.contains(6)) {
                        filteredSMSList.add(SMS_Item("", "6 hours ago", "", "", 1))
                    }
                    headersList.add(6)
                } else if (it in 12..24) {
                    if (!headersList.contains(12)) {
                        filteredSMSList.add(SMS_Item("", "12 hours ago", "", "", 1))
                    }
                    headersList.add(12)
                } else if (it in 24..48) {
                    if (!headersList.contains(24)) {
                        filteredSMSList.add(SMS_Item("", "1 day ago", "", "", 1))
                    }
                    headersList.add(24)
                }

                for (sms in smsListDatemap!![it]!!) {
                    filteredSMSList.add(sms)
                }
            }
        }

        val smsAdapter = RVASMS(filteredSMSList)
        binding.rvSMS.adapter = smsAdapter
    }

    private fun filterSMSList(smsList: ArrayList<SMS_Item>) {
        smsListDatemap = HashMap()
        smsList.forEach {
            val key: Long = (System.currentTimeMillis() - it.dateTIme.toLong()) / (1000 * 60 * 60)
            if (smsListDatemap!!.containsKey(key)) {
                smsListDatemap!![key]!!.add(it)
            } else {
                smsListDatemap!![key] = arrayListOf(it)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            myPermissionsRequestReadSms -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_SMS
                        )
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        initSMSList()
                        return
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        "permission denied",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
            myPermissionsRequestReceiveSms -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.RECEIVE_SMS
                        )
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        "permission denied",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
        }

    }

    private fun checkSmsPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_SMS)) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS),
                    myPermissionsRequestReadSms
                )
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS),
                    myPermissionsRequestReadSms
                )
            }
            false
        } else {
            true
        }
    }
}
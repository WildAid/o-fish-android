package org.wildaid.ofish.util

import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.util.*

class AndroidPermissions(
    private val activity: FragmentActivity,
    private val fragment: Fragment? = null
) {
    fun requestPermissions(requestCode: Int, vararg permissions: String) {
        if (fragment != null) {
            fragment.requestPermissions(permissions, requestCode)
        } else {
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        }
    }

    fun checkNonGrantedPermissions(vararg requestedPermissions: String): Array<String> {
        val nonGrantedPermissions: MutableList<String> = ArrayList()
        for (permission in requestedPermissions) {
            if (!isPermissionGranted(permission)) {
                nonGrantedPermissions.add(permission)
            }
        }
        return nonGrantedPermissions.toTypedArray()
    }

    fun isPermissionGranted(permission: String?): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            permission!!
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isPermissionsGranted(vararg permissions: String?): Boolean {
        for (permission in permissions) {
            if (!isPermissionGranted(permission)) {
                return false
            }
        }
        return true
    }

    fun getPermissionsNonGrantedFromRequest(
        permissions: Array<String>,
        grantResults: IntArray
    ): Array<String> {
        val permissionList: MutableList<String> = ArrayList()
        for (i in permissions.indices) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                permissionList.add(permissions[i])
            }
        }
        return permissionList.toTypedArray()
    }

    fun getPermissionsNonGrantedFromRequest(
        permissions: Array<String>, grantResults: IntArray,
        vararg requestedPermissions: String
    ): Array<String> {
        val permissionList: MutableList<String> =
            ArrayList()
        for (permission in requestedPermissions) {
            if (!isPermissionsGrantedFromRequest(permissions, grantResults, permission)) {
                permissionList.add(permission)
            }
        }
        return permissionList.toTypedArray()
    }

    fun isPermissionsGrantedFromRequest(
        permissions: Array<String>, grantResults: IntArray,
        queriedPermission: String
    ): Boolean {
        for (i in permissions.indices) {
            if (permissions[i] == queriedPermission && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                return true
            }
        }
        return false
    }

    private fun getPermissionsDeniedPermanently(vararg permissions: String): Array<String> {
        val permissionsShouldShowRationale: MutableList<String> = ArrayList()
        for (permission in permissions) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    permission
                )
            ) {
                permissionsShouldShowRationale.add(permission)
            }
        }
        return permissionsShouldShowRationale.toTypedArray()
    }

    fun isPermissionsDeniedPermanently(permissions: Array<String>): Boolean {
        return getPermissionsDeniedPermanently(*permissions).isNotEmpty()
    }
}
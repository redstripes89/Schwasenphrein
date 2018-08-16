package de.redstripes.schwasenphrein

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SignUpFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [SignUpFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class SignUpFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null

    private var textName: EditText? = null
    private var textMail: EditText? = null
    private var textPassword: EditText? = null
    private var btnSignUp: Button? = null
    private var progressBar: ProgressBar? = null

    private var databaseReference: DatabaseReference? = null
    private var database: FirebaseDatabase? = null
    private var auth: FirebaseAuth? = null

    private var name: String? = null
    private var mail: String? = null
    private var password: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = FirebaseDatabase.getInstance()
        databaseReference = database!!.reference!!.child("users")
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textName = view.findViewById(R.id.sign_up_name) as EditText
        textMail = view.findViewById(R.id.sign_up_mail) as EditText
        textPassword = view.findViewById(R.id.sign_up_password) as EditText
        btnSignUp = view.findViewById(R.id.sign_up_btn) as Button

        btnSignUp!!.setOnClickListener { createNewAccount() }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        progressBar = ProgressBar(context)

        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment SignUpFragment.
         */
        @JvmStatic
        fun newInstance() = SignUpFragment()
    }

    private fun createNewAccount(){
        if(!validateForm()){
            return
        }
    }

    private fun validateForm(): Boolean {
        var result = true

        name = textName?.text.toString()
        mail = textMail?.text.toString()
        password = textPassword?.text.toString()

        if(TextUtils.isEmpty(name)) {
            textName?.error = "Required"
            result = false
        } else{
            textName?.error = null
        }

        if(TextUtils.isEmpty(mail)) {
            textMail?.error = "Required"
            result = false
        } else {
            textMail?.error = null
        }

        return result
    }

    private fun showProgress() {
        if(progressBar == null) {
            progressBar = ProgressBar(context)
        }

        progressBar?.visibility = View.VISIBLE
        activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private fun hideProgress() {

    }
}

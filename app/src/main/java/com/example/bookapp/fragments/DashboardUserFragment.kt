package com.example.bookapp.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.findNavController
import androidx.viewpager.widget.ViewPager
import com.example.bookapp.R
import com.example.bookapp.databinding.FragmentDashboardUserBinding
import com.example.bookapp.models.CategoryData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardUserFragment : Fragment() {
    private lateinit var binding: FragmentDashboardUserBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var categoryArrayList : ArrayList<CategoryData>
    private lateinit var viewPagerAdapter : ViewPagerAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentDashboardUserBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth= FirebaseAuth.getInstance()
        checkUser()
        setupWithViewPagerAdapter (binding.ViewPager)
        binding.tablayout.setupWithViewPager(binding.ViewPager)
        binding.btnLogoutUser.setOnClickListener {
            auth.signOut()
            checkUser()
        }
        binding.profileBtn.setOnClickListener {
            view.findNavController().navigate(R.id.action_dashboardUserFragment_to_profileFragment)

        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun setupWithViewPagerAdapter(viewPager: ViewPager) {
        viewPagerAdapter = this.context?.let {
            ViewPagerAdapter(
                parentFragmentManager,
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, it
            )
        }!!

        categoryArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                categoryArrayList.clear()

                val modelAll = CategoryData("01", "Բոլորը", 1, "")
                val modelMostViewed = CategoryData("01", "Ամենադիտված", 1, "")
                val modelMostDownloaded = CategoryData("01", "Ամենաներբեռնված", 1, "")

                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewed)
                categoryArrayList.add(modelMostDownloaded)
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelAll.id}",
                        "${modelAll.category}",

                        "${modelAll.uid}"
                    ), modelAll.category
                )
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostViewed.id}",
                        "${modelMostViewed.category}",

                        "${modelMostViewed.uid}"
                    ), modelMostViewed.category
                )


                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostDownloaded.id}",
                        "${modelMostDownloaded.category}",

                        "${modelMostDownloaded.uid}"
                    ), modelMostDownloaded.category
                )
                viewPagerAdapter.notifyDataSetChanged()
                for (ds in snapshot.children) {

                    val model = ds.getValue(CategoryData::class.java)

                    categoryArrayList.add(model!!)

                    viewPagerAdapter.addFragment(
                        BooksUserFragment.newInstance(
                            "${model.id}",
                            "${model.category}",
                            "${model.uid}"
                        ), model.category
                    )
                    viewPagerAdapter.notifyDataSetChanged()
                }
            }


            override fun onCancelled(error: DatabaseError) {

            }
        })
        viewPager.adapter = viewPagerAdapter
    }

    class ViewPagerAdapter(fn: FragmentManager, behavior: Int, context: Context): FragmentPagerAdapter(fn, behavior) {

        private val fragmentsList: ArrayList<BooksUserFragment> = ArrayList()

        private val fragmentTitleList: ArrayList<String> = ArrayList()
        private val context: Context

        init {
            this.context = context
        }

        override fun getCount(): Int {
            return fragmentsList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentsList[position]
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList[position]
        }

        public fun addFragment(fragment: BooksUserFragment, title: String) {
            fragmentsList.add(fragment)
            fragmentTitleList.add(title)
        }
    }



    private fun checkUser() {
        val user=auth.currentUser
        if(user==null){
            binding.userEmail.text="Ոչ ոք"
            binding.profileBtn.visibility = View.GONE
            binding.btnLogoutUser.visibility = View.GONE
            view?.findNavController()?.navigate(R.id.action_dashboardUserFragment_to_betweenFragment)
        }
        else{
            val email=user.email
            binding.userEmail.text=email
            binding.profileBtn.visibility = View.VISIBLE
            binding.btnLogoutUser.visibility = View.VISIBLE
        }
    }
}
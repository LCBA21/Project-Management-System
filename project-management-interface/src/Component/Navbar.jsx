import React, { useEffect, useState } from 'react';
import axios from 'axios'; 
import imageOne from "../assets/topnav-image1.jpg";
import Cookies from 'js-cookie';
import { Link } from 'react-router-dom';
import NotificationDropDown from './NotificationDropdown';


function Navbar() {
  const [siteUser, setSiteUser] = useState();
  const [name, setName] = useState();
  const [logUser, setLogUser] = useState();
  const [userId, setUserId] = useState();
  const [dropDownBoxesHeight, setDropDownBoxesHeight] = useState({
    statusBox: 0, addColumn: 0, columnIndex: 0, editRow: 0, inviteBox: 0,
    filterBox: 0, sort: 0, rowId: 0, statusIndex: 0, table: 0, tableIndex: 0,
    commentBox: 0, commentTaskId: 0, assigneeHieght: 0, assigneeId: 0
  });

  useEffect(() => {
    setSiteUser(JSON.parse(sessionStorage.getItem("systemUser")));
    const foundUser = (JSON.parse(sessionStorage.getItem("systemUser")));
    setLogUser(foundUser)
    return () => {
    }
  }, []);

  const toogleDropDownBoxes = (contentType, height, rowIndex, indexType) => {
    setDropDownBoxesHeight(prevState => ({
      ...prevState,
      [contentType]: dropDownBoxesHeight[contentType] === 0 ? height : 0,
      [indexType !== "" ? indexType : '']: rowIndex
    }));
    const ame = siteUser.fullname
    const letter = ame.charAt(0)
    console.log(letter)
    setName(letter)
  }

  const handleDeactivateAccount = (e) => {
    e.preventDefault(); 
     setUserId(siteUser.user_id)

    // Send an AJAX request to deactivate the account
    axios.put(`/user/deactivate/${userId}`,{}, { 
      headers: {
        Authorization: `Bearer ${logUser.token}`, // Assuming token is stored in a variable
        'Content-Type': 'application/json'
    }
     })
      .then(response => {
        // Handle success response if needed
        console.log('Account deactivated successfully');
      })
      .catch(error => {
        // Handle error response if needed
        console.error('Error deactivating account:', error);
        console.log(userId)
      });
  };

  const logOut = async () => {
    try {
      const response = await axios.delete("http://localhost:8080/user/logoutUser", {
        withCredentials:true
      })
      if (response == "user logged out") {
        Cookies.remove('jwtToken')
        Cookies.remove("refreshToken")
        window.location.href = "/"
      } else {
        console.log(response.data)
      }
    } catch(error) {
      console.error("user could'nt be logged out",error)
    }
  }
  const [openProfile, setOpenProfile] = useState(false);
  return (
    <>
      <div className="navbar">
        <div className="page-row">
          <form action=""><input type="text" placeholder='Search here' />
            <button id='input_search_btn'><i className="lni lni-search"></i></button></form>
          <div className="nav-right-bar">
            <div className="nav-icons">
              <i className="lni lni-information"></i>
              <i className="lni lni-alarm" onClick={()=> setOpenProfile((prev)=> !prev)}></i>
                {
                    openProfile && <NotificationDropDown />
                }
                    <Link to="/profileEditPage">
                        <i className="lni lni-cog" ></i>
                    </Link>
            </div>
            <div className="profile" >
              <div>
                <span>{siteUser ? siteUser.fullname : ""}</span>
                <span>{siteUser ? siteUser.email : ""}</span>
              </div>
              <img src={imageOne} onClick={() => toogleDropDownBoxes("inviteBox", 400, 0, "")} alt="" />
              <div style={{ height: dropDownBoxesHeight.inviteBox }} className="profile_popup invite_members">
                <div className="member_invite_wrapper">
                  <h5 style={{ fontWeight: "bold" }}>ACCOUNT</h5>
                  <div style={{ position: "relative" }}>
                    <div className="project_create_table_invite">
                      <div className="task_assign_letter">{siteUser ? siteUser.email.charAt(0) : ""}</div>
                      <span>{siteUser ? siteUser.fullname : ""}</span>
                      <span>{siteUser ? siteUser.email : ""}</span>
                    </div>
                  </div>
                  <a href='/switch' id="account_switch">Switch account</a>
                  <h5 style={{ fontWeight: "bold" }}>ProjectGuru</h5>
                  <div className="sidebar-links profile_links">
                    <a href='/profilepage' >Password reset</a>
                    <a href='/disable'>Deactivate account</a>
                    <a href='/profileEditPage'>Settings</a>
                    <a href='/feedbackpage'>Help</a>
                    <a id="logout" href='/'>Logout</a>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}

export default Navbar;

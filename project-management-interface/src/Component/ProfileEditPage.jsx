// import './styles/ProfileEditPage_Style.css';
import { useNavigate } from 'react-router-dom';
import InsideNavBar from './InsideNavBar';
import Navbar from './Navbar';
import Sidebar from './Sidebar';
import { useState } from 'react';

function ProfileEditPage() {
    
 
    return (
        <div className="page-row">
            <Sidebar />
            <div className="project-wrapper">
                <Navbar />
                <div className="View_Container settings_container">
                    <div className="Nav_Holder">
                        <InsideNavBar />
                    </div>
                     <div className="View_Elements_Container settings_sections">
                            <div className="settings_header">
                                <h1> <i className="lni lni-users"></i>View Profile</h1>
                                <p> Manage your Name, and Email Addresses</p>
                                <p> Below are the name and email addresses on file for your account.</p>
                            </div>
                            <div className="table_section">
                                <div className="View_Table_section_name"><p>Your UserName:</p></div>
                                <h6>3</h6>
                                {/* <div className="View_Table_section_name"><BiEdit className="icon" onClick={redirectTo} /></div> */}
                            </div>
                            <div className="table_section">
                                <div className="View_Table_section_name"><p>Your Number of Tables</p></div>
                                <h6>1</h6>
                                {/* <div className="View_Table_section_name"><CiViewTable className="icon" /></div> */}
                            </div>
                            <div className="table_section">
                                <div className="View_Table_section_name"><p>Your Number of projects</p></div>
                                <h6>4</h6>
                                {/* <div className="View_Table_section_name"><FaProjectDiagram className="icon" /></div> */}
                            </div>
                            <div className="table_section">
                                <div className="View_Table_section_name"><p>Your Number of Tasks</p></div>
                                <h6>3</h6>
                                {/* <div className="View_Table_section_name"><FaTasks className="icon" /></div> */}
                            </div>
                            <div className="settings-buttons View_Table_section_Button_Section">
                                <button >Deactivate account</button>
                            </div>
                        </div>
                </div>
            </div>
        </div>
    );
}

export default ProfileEditPage;

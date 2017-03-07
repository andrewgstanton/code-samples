<?php
class UsersController extends AppController {

	var $name = 'Users';

        var $components = array('Session','Auth'); // Not necessary if declared in your app controller

        function beforeFilter() {
           $this->Auth->allow('*');
           $this->Auth->autoRedirect = false;
           Configure::write('debug',0);
        }

        function index() {
		$this->User->recursive = 0;
		$this->set('users', $this->paginate());
	}

	function view($id = null) {
		if (!$id) {
			$this->Session->setFlash(__('Invalid user', true));
			$this->redirect(array('action' => 'index'));
		}
		$this->set('user', $this->User->read(null, $id));
	}

	function add() {
		if (!empty($this->data)) {
			$this->User->create();
			if ($this->User->save($this->data)) {
				$this->Session->setFlash(__('The user has been saved', true));
				$this->redirect(array('action' => 'index'));
			} else {
				$this->Session->setFlash(__('The user could not be saved. Please, try again.', true));
			}
		}
	}

	function edit($id = null) {
		if (!$id && empty($this->data)) {
			$this->Session->setFlash(__('Invalid user', true));
			$this->redirect(array('action' => 'index'));
		}
		if (!empty($this->data)) {
			if ($this->User->save($this->data)) {
				$this->Session->setFlash(__('The user has been saved', true));
				$this->redirect(array('action' => 'index'));
			} else {
				$this->Session->setFlash(__('The user could not be saved. Please, try again.', true));
			}
		}
		if (empty($this->data)) {
			$this->data = $this->User->read(null, $id);
		}
	}

	function delete($id = null) {
		if (!$id) {
			$this->Session->setFlash(__('Invalid id for user', true));
			$this->redirect(array('action'=>'index'));
		}
		if ($this->User->delete($id)) {
			$this->Session->setFlash(__('User deleted', true));
			$this->redirect(array('action'=>'index'));
		}
		$this->Session->setFlash(__('User was not deleted', true));
		$this->redirect(array('action' => 'index'));
	}


        function login() {
           $this->delete_user_parms();
           if ($this->Session->read('Auth.User')) {
              $this->get_user_parms();
              $this->redirect('/profile', null, false);
            }
        }

       function delete_user_parms() {
         $this->Session->delete('item_count');
         $this->Session->delete('inbox_count');

       }
       function get_user_parms() {
	   if ($this->Session->read('Auth.User')) {
                 $user = $this->Session->read('Auth.User');
                 $user_id = $user['id'];

                 // add items from session cart to user's cart 
                 // turn off error reporting -- if record is already there
                 // it won't be added again, but this generates a warning
                 // and a sql error
                 error_reporting(0);
                 $session_cart = $this->Session->read('session_cart');
                 if (!empty($session_cart['Items'])) {
                       for ($i=0;$i<$session_cart['item_count'];$i++) {
                          $video_id = $session_cart['Items'][$i]['Item']['id'];
                          // make sure video is not owned by me...
                          $qry = "SELECT * from videos as Video WHERE Video.id = " . $video_id;
                          //echo "qry = " . $qry . "<br />\n";
                          $video_user = $this->User->query($qry);
                          $video_user_id = $video_user[0]['Video']['user_id'];
                          if ($video_user_id != $user_id ) {
                             // add schedule section if item is a live video
                             if (!empty($session_cart['Items'][$i]['Schedule']['id'])) {
                                 $schedule_id = $session_cart['Items'][$i]['Schedule']['id'];
                                 $qry = "INSERT INTO items (video_id, user_id, schedule_id, is_bought) ";
                                 $qry .= " VALUES (" . $video_id . "," . $user_id . "," . $schedule_id . ",0) ";
                             } else {
                                 $qry = "INSERT INTO items (video_id, user_id, is_bought) ";
                                 $qry .= " VALUES (" . $video_id . "," . $user_id .  ",0) ";
                             }
                             $this->User->query($qry); 
                          }
                       } 
                 }
                 // after copying info into items table, delete session cart
                 $this->Session->delete('session_cart');

                 // select items in cart
                 $qry = "SELECT count(Item.id) as item_count FROM items as Item ";
                 $qry .= " JOIN users as User on User.id= Item.user_id ";
                 $qry .= " WHERE User.id = " . $user_id . " AND Item.is_bought = 0 ";
                 $items = $this->User->query($qry);
                 $item_count = $items[0][0]['item_count'];

                 // select inbox items
                 $qry = "SELECT count(Inboxmessage.id) as inbox_count FROM inboxmessages as Inboxmessage ";
                 $qry .= " JOIN users as User on User.id= Inboxmessage.user_id ";
                 $qry .= " WHERE User.id = " . $user_id;
                 $messages = $this->User->query($qry);
                 $inbox_count = $messages[0][0]['inbox_count'];

                 // print_r($items);

                 $this->Session->write('item_count',$item_count);
                 $this->Session->write('inbox_count',$inbox_count);
  
           }
       }
       function logout() {
           $this->delete_user_parms();
           $this->Auth->logout();
           $this->redirect('/',null,false);
       }

      function signup() {
        if ($this->data) {
           $this->register();
           $this->redirect('/',null,false);
        }
     }

     function register() {
        if ($this->data) {
           if ($this->data['User']['password'] == $this->Auth->password($this->data['User']['confirm_password']) && $this->data['User']['confirm_password'] != '') {
               $this->User->create();
               $this->User->save($this->data);
               $this->Auth->login($this->data);
           }
        }
     }
     function update_profile() {
        if (!empty($this->data)) {
            $user = $this->Session->read('Auth.User');
            $user_id = $user['id'];
            $qry = "UPDATE users as User ";
            $qry .= " SET User.first_name = '" . $this->data['User']['first_name'] . "', ";
            $qry .= " User.last_name = '" . $this->data['User']['last_name'] . "', ";
            $qry .= " User.display_name = '" . $this->data['User']['display_name'] . "' ";
            if ($this->data['User']['password'] == $this->Auth->password($this->data['User']['confirm_password']) && $this->data['User']['confirm_password'] != '') {
               $qry .= ", User.password = '" . $this->data['User']['password'] . "' ";
            }
            $qry .= ", User.is_seller = " . $this->data['User']['is_seller'];
            $qry .= " WHERE User.id = " . $user_id;
            $this->User->query($qry);
            // update session variables
            $this->Session->write('Auth.User.first_name', $this->data['User']['first_name']);
            $this->Session->write('Auth.User.last_name', $this->data['User']['last_name']);
            $this->Session->write('Auth.User.display_name', $this->data['User']['display_name']);
            $this->Session->write('Auth.User.is_seller', $this->data['User']['is_seller']);

        }
     }

     function update_buyer_profile() {
        $this->update_profile();
        $this->redirect('/profile', null, false);
      }
     function update_seller_profile() {
        $this->update_profile();
        // if we disable seller profile, redirect to buyer profile (which will hide the seller profile tab)
        if ($this->data['User']['is_seller'] == 0) {
           $this->redirect('/profile', null, false);
        } else {
           $this->redirect('/sellerProfile', null, false);
        }
     } 

      
     // get information about the user, like order history, etc...

     function profile() {
        $user = $this->Session->read('Auth.User');
        $user_id = $user['id'];
        // select items in cart that were ordered
        $qry = "SELECT * FROM items as Item ";
        $qry .= " JOIN users as User on User.id= Item.user_id  AND Item.is_bought = 1 ";
        $qry .= " JOIN videos as Video on Video.id = Item.video_id ";
        $qry .= " JOIN categories as Category on Category.id = Video.category_id ";
        $qry .= " LEFT OUTER JOIN schedules as Schedule on Schedule.id = Item.schedule_id ";
        $qry .= " WHERE User.id = " . $user_id;
        //echo "qry = ". $qry;
        $order_history = $this->User->query($qry);
        //print_r($order_history);

        $this->set('order_history',$order_history);

        // select favorites
        $qry = "SELECT * from favorites as Favorite ";
        $qry .= " JOIN users as FavoriteUser on FavoriteUser.id = Favorite.favorite_id ";
        $qry .= " WHERE Favorite.user_id = " . $user_id;
 
        $favorites = $this->User->query($qry);
        $this->set('favorites', $favorites);

     //   print_r($favorites);

 
     } 

     function sellerProfile() {

        $user = $this->Session->read('Auth.User');
        $user_id = $user['id'];
     // select presentations
      $date = date('Y-m-d');
      
      $qry = " SELECT * from schedules as Schedule ";
      $qry .= " JOIN videos as Video on Video.id = Schedule.video_id ";
      $qry .= " JOIN users as User on User.id = Video.user_id ";
      $qry .= " WHERE User.id = " . $user_id . " AND Schedule.date >= '" . $date . "' ORDER BY Schedule.date DESC ";
      

      $scheduled_shows = $this->User->query($qry);

      $this->set('scheduled_shows', $scheduled_shows);

     }

     function sellerSchedule() {

        $display_name = str_replace(GSM_ROOT . "sellerSchedule/", "",  $_SERVER['REQUEST_URI']);

        $qry = "SELECT * FROM users as User ";
        $qry .= " WHERE User.display_name = '" . $display_name . "' LIMIT 1";

        $user = $this->User->query($qry);
        //$this->set('user',$user);
        if (empty($user) || $user[0]['User']['is_seller'] == 0) {
           $this->redirect('/search',null,false);
        }         

        $video_id = $this->data['Item']['video_id'];
        
        // get the items in the shopping cart already scheduled
        if ($this->Session->read('Auth.User')) {
           $user = $this->Session->read('Auth.User');
           $user_id = $user['id'];
           $qry = " SELECT * from items as Item ";
           $qry .= " WHERE Item.video_id = " . $video_id . " AND Item.is_bought = 0 ";
           $qry .= " AND Item.user_id = " . $user_id;

           $already_scheduled = $this->User->query($qry);
           $this->set('already_scheduled',$already_scheduled);
        }
 
        // get the available time slots

        // if the user is valid, continue on to get the available time slots for the show
        $qry = "SELECT * from schedules as Schedule ";
        $qry .= " WHERE Schedule.video_id = " . $video_id . " ORDER BY Schedule.date ASC, Schedule.time ASC ";

        // get the video details

        $schedule = $this->User->query($qry);
     
        $qry = "SELECT * from videos as Video ";
        $qry .= "JOIN users as User on User.id = Video.user_id ";
        $qry .= " WHERE Video.id = " . $video_id;
       
        $video = $this->User->query($qry);
 
        $this->set('video',$video);
        $this->set('schedule',$schedule);

     }

     // if no match, or not a seller, redirect to home page
     function sellerPublicProfile() {
        
        $request_uri = str_replace(GSM_ROOT . "sellerPublicProfile/", "",  $_SERVER['REQUEST_URI']);

        $request_parms = split("/",$request_uri);
        $display_name = $request_parms[0];
        if (sizeof($request_parms) > 1) {
          $current_video_id = $request_parms[1];
        }
 

        $qry = "SELECT * FROM users as User ";
        $qry .= " WHERE User.display_name = '" . $display_name . "' LIMIT 1";
        $user = $this->User->query($qry);

        $this->set('user',$user);

        // select the videos for the user 
        $qry = "SELECT * FROM videos as Video ";
        $qry .= " JOIN categories as Category ON Category.id = Video.category_id ";
        $qry .= " JOIN users as User WHERE User.id = Video.user_id AND User.display_name = '" . $display_name . "'";
        $videos = $this->User->query($qry);
        $this->set('videos', $videos);

        // select the current video

       if (!empty($current_video_id)) { 
          $qry = " SELECT * FROM videos as Video ";
          $qry .= " JOIN categories as Category ON Category.id = Video.category_id ";
          $qry .= " JOIN users as User ON User.id = Video.user_id ";
          $qry .= " WHERE Video.id = " . $current_video_id;
          $current_video = $this->User->query($qry);
          $this->set('current_video', $current_video);

          // schedules for this video
          $qry = "SELECT * FROM schedules as Schedule ";
          $qry .= " JOIN videos as Video ON Video.id = Schedule.video_id ";
          $qry .= " WHERE Video.id = " . $current_video_id;
          $qry .= " ORDER BY Schedule.date ASC, Schedule.time ASC ";
          $current_schedule = $this->User->query($qry);
          $this->set('current_schedule', $current_schedule);

         //print_r($current_video);

       }
//        print_r($videos);

        if (empty($user) || $user[0]['User']['is_seller'] == 0) {
           $this->redirect('/',null,false);
        }         
     }

     // manage the user's public profile, if not logged in redirect to home page
     function managePublicProfile() {

       //print_r($this->data);

       $user = null;
       $user_id = "";

       if (!$this->Session->read('Auth.User')) {
           $this->redirect('/', null, false);
       } else {
          $user = $this->Session->read('Auth.User');
          $user_id = $user['id'];
       }

       if (!empty($this->data)) {
          $video_id = $this->data['Video']['id'];
          $video_title = $this->data['Video']['title'];
          $video_description = $this->data['Video']['description'];
          $video_price = $this->data['Video']['price'];
          $video_participants = $this->data['Video']['participants'];
          $video_length = $this->data['Video']['length'];

          
          $sql = "UPDATE videos SET title = '" . mysql_real_escape_string($video_title) . "', ";
          $sql .= "description = '" . mysql_real_escape_string($video_description) . "', ";
          $sql .= "price = '" . mysql_real_escape_string($video_price) . "', ";
          $sql .= "participants = '" . mysql_real_escape_string($video_participants) . "', ";
          $sql .= "length = '" . mysql_real_escape_string($video_length) . "' ";
          $sql .= " WHERE id = "  . $video_id;

          $this->User->query($sql);
          $this->redirect('/managePublicProfile/' . $video_id, null,false);
       }
     
       // get the current video to edit
       $current_video_id_str = str_replace(GSM_ROOT . "managePublicProfile/", "",  $_SERVER['REQUEST_URI']);

       $current_video_id = $current_video_id_str;

       // if adding a video
       if ($current_video_id_str == "create") {
        $this->set('create', 'true');
       } else {
          // check if there are other parameters after the video id (for deleting)
          $current_video_array = split("/", $current_video_id_str);
         if (!empty($current_video_array)) {
           if (sizeof($current_video_array) > 1) {
               $current_video_id = $current_video_array[0];
               if ($current_video_array[1] == "delete") {
                 $qry = "DELETE FROM videos WHERE id = " . $current_video_id;
                 $this->User->query($qry);
                 $this->redirect('/managePublicProfile', null, false);
               }  
           } 
         }
       }
 
 
       if (!empty($current_video_id)) { 
          $qry = " SELECT * FROM videos as Video ";
          $qry .= " JOIN categories as Category ON Category.id = Video.category_id ";
          $qry .= " JOIN users as User ON User.id = Video.user_id ";
          $qry .= " WHERE Video.id = " . $current_video_id;
          $current_video = $this->User->query($qry);
          $this->set('current_video', $current_video);

          // schedules for this video
          $qry = "SELECT * FROM schedules as Schedule ";
          $qry .= " JOIN videos as Video ON Video.id = Schedule.video_id ";
          $qry .= " WHERE Video.id = " . $current_video_id;
          $qry .= " ORDER BY Schedule.date ASC, Schedule.time ASC ";
          $current_schedule = $this->User->query($qry);
          $this->set('current_schedule', $current_schedule);
       }

       $qry = " SELECT * FROM videos as Video ";
       $qry .= " JOIN categories as Category ON Category.id = Video.category_id ";
       $qry .= " JOIN users as User ON User.id = Video.user_id ";
       $qry .= " WHERE User.id = " . $user_id;
     
       $videos = $this->User->query($qry);
       $this->set('videos', $videos);
  
     }
     // for checking out a user for a new account
     function checkoutNewUser() {
       $this->register();
       $this->get_user_parms();
       $this->RequestAction('/items/checkout');
       if ($this->Session->read('Auth.User')) {
           $this->redirect('/profile/', null, false);
       } else { 
           $this->redirect('/cart/', null, false);
       }
     }

     // for adding a favorite
     function addFavorite() {
       $user_id = $_POST['user_id'];
       $favorite_id = $_POST['favorite_id'];
       $qry = "INSERT INTO favorites (user_id, favorite_id) VALUES (" . $user_id . "," . $favorite_id . ")";
       $this->User->query($qry);
       $this->redirect('/', null, false);
 
     }
}
?>

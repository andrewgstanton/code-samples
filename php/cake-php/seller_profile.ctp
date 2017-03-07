<!-- <?php echo(__FILE__); ?> -->

<?php

$user = $this->Session->read('Auth.User');
//print_r($scheduled_shows);

?>


			<div class="floatLeft w100p">
				<h2 class="profile inactive"><a href='<?php echo GSM_ROOT ?>profile'>Buyer Profile</a></h2>
                                <h2 class="profile"><a href='<?php echo GSM_ROOT ?>sellerProfile'>Seller Profile</a></h2>
				<div id="panel1" class="profileBg">
					<div class="p20">
						<div class="w50p floatLeft">
							<h3>Seller Profile Details</h3> 
                                                        <a href='<?php echo GSM_ROOT ?>managePublicProfile'>Manage Public Profile</a>&nbsp;&nbsp; 
                                                        <a href='<?php echo GSM_ROOT ?>sellerPublicProfile/<?php echo $user['display_name'] ?>'>View Public Profile</a>

							
							<?php echo $form->create('User', array('class' => '','controller'=>'users', 'action'=>'update_seller_profile')); ?>
									<?php echo $form->input('username', array('class' => 'required','value'=>$user['username'])); ?>
									<?php echo $form->input('first_name', array('class' => 'required', 'value'=>$user['first_name'])); ?>
									<?php echo $form->input('last_name', array('class' => 'required','value'=>$user['last_name'])); ?>
									<?php echo $form->input('display_name', array('class' => 'required','value'=>$user['display_name'])); ?>
									<div class="input text">
										<label>Password</label>
										<?php echo $form->password('password', array('class' => 'required')); ?>
									</div>
									<div class="input text">
										<label>Confirm Password</label>
										<?php echo $form->password('confirm_password', array('class' => 'required')); ?>
									</div>
									<div class="input text">
										<label>Address</label>
										<?php echo $form->textarea('address', array('class' => '')); ?>
									</div>
									<div class="input text">
										<label>Sure, an OCCASIONAL email is fine</label>
										<?php echo $form->checkbox('newsletter', array('class' => '')); ?>
									</div>
									<div class="input text">
										<label>Seller profile enabled?</label>
                                                                                <?php $is_seller = ($user['is_seller'] == 1) ? 'true' : 'false'; ?>
									        <?php echo $form->checkbox('is_seller', array('class' => '','checked'=>$is_seller));  ?>
									</div>
									<div class="input text">
										<label>&nbsp;</label>
										<?php echo $form->end('Update Account'); ?>
									
								</div>
							<div class="floatLeft">
								<h3>Current Balance</h3>
								<div class="p20 bold clearBoth">
									<div class="floatLeft">
										&curren;1,000
									</div>
									<div class="floatLeft ml4">
										<img src="<?php echo GSM_IMAGES ?>leaf.png" />
									</div>
									<div class="floatLeft ml20">
										<a href="#">add funds</a> | <a href="#">withdraw funds</a>
									</div>
								</div>
								<em class="smaller">conversion: &curren;1000 Leaves = $80USD </em>
							</div>	
							<div class="floatLeft clearBoth mt15">
								<div class="floatLeft h27"><img src="<?php echo GSM_IMAGES ?>calendar.png" /></div>
								<div class="floatLeft h27 ml20"><a href="<?php echo GSM_PAGES ?>calendar">View my presentation calendar</a></div>
							</div>			
						</div>
						<div class="w50p floatLeft">

                                                        <h3>My Reviewed Videos</h3>
                                                        <div class="p20">
                                                                <ul>
                                                                        <li><a class="floatLeft" href="#">King Kong</a></li>
                                                                        <li><a href="#">Spider Man</a></li>
                                                                        <li><a href="#">Wonder Woman</a></li>
                                                                        <li><a href="#">Handy Manny</a></li>
                                                                        <li><a href="#">The Care Bears</a></li>
                                                                        <li><a href="#">The Smurfs</a></li>
                                                                </ul>
                                                        </div>
<!--
                                                        <h3>My Upcomming Shows</h3>
                                                        <div class="p20">
                                                                <a href="#calendar">view in calendar</a>
                                                                <ul>
                                                                        <li><span class="block floatLeft" style="width:200px;"><a href="#">King Kong</a></span>purchasers: <a href="#">(3)</a></li>
                                                                        <li><span class="block floatLeft" style="width:200px;"><a href="#">Spider Man</a></span>purchasers: (0)</li>
                                                                        <li><a href="#">Wonder Woman</a></li>
                                                                        <li><a href="#">Handy Manny</a></li>
                                                                        <li><a href="#">The Care Bears</a></li>
                                                                        <li><a href="#">The Smurfs</a></li>
                                                                </ul>
                                                        </div>
-->

                                                        <?php if (!empty($scheduled_shows)) { ?>
                                                        <h3>My Upcoming Shows</h3>

                                                        <div class="p20">
                                                           <ul>
                                                           <?php  foreach ($scheduled_shows as $show) {

                                                            // print_r($show);

 
                                                              $schedule_id = $show['Schedule']['id'];
                                                              $title = $show['Video']['title'];
                                                              $video_id = $show['Video']['id'];
                                                              $date_playing = $show['Schedule']['date'];
                                                              $chat_time = $show['Schedule']['time'];

                                                              $chat_date = date('F d, Y' , strtotime($date_playing));
                                                              echo "<li><a href='" . GSM_ROOT  . "liveChatPresenter/" . $schedule_id . "'>" . $title . " - " . $chat_date . " " . $chat_time . "</a></li>\n";
                                                             } ?>
                                                           </ul>
                                                        </div>

                                                        <?php } ?>
							<h3>Favorite Sellers</h3>
							<div class="p20">
								<ul>
									<li><a href="#">King Kong</a></li>
									<li><a href="#">Spider Man</a></li>
									<li><a href="#">Wonder Woman</a></li>
									<li><a href="#">Handy Manny</a></li>
									<li><a href="#">The Care Bears</a></li>
									<li><a href="#">The Smurfs</a></li>
								</ul>
							</div>
						</div>
						<div class="clearBoth"></div>
					</div>
				</div>
			</div>

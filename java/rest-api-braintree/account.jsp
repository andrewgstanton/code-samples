<%@include file="nav_header.jsp" %>
<%
String login = (request.getParameter("login") != null) ? request.getParameter("login") : "";
String owner_id = "";
Site site = new Site();
String ownerName = site.getOwnerNameFromSiteId(sc.getEditingSiteId());
Site site2 = new Site(sc.getEditingSiteId(), ownerName);
String current_account_plan = site2.getSiteAccountPlan();
String active = (site2.getSiteStatus().equals("1")) ? "Active" : "Disabled";
String account_plan_sql = "SELECT * FROM webchalet_account_types wp ORDER BY wp.id ASC";
ResultSet account_plan_rs = null;
String newPasswordSubmitted = (request.getParameter("new-password") != null) ? request.getParameter("new-password") : "";
String newPasswordSubmittedConfirm = (request.getParameter("new-password-repeat") != null) ? request.getParameter("new-password-repeat") : "";
String account_plan = (request.getParameter("account_plan") != null) ? request.getParameter("account_plan") : "";
String first_name = (request.getParameter("firstname") != null) ? request.getParameter("firstname") : "";
String last_name = (request.getParameter("lastname") != null) ? request.getParameter("lastname") : "";
String phone1 = (request.getParameter("phone1") != null) ? request.getParameter("phone1") : "";
String phone2 = (request.getParameter("phone2") != null) ? request.getParameter("phone2") : "";
String phone3 = (request.getParameter("phone3") != null) ? request.getParameter("phone3") : "";
String phone = phone1+phone2+phone3;
String fax1 = (request.getParameter("fax1") != null) ? request.getParameter("fax1") : "";
String fax2 = (request.getParameter("fax2") != null) ? request.getParameter("fax2") : "";
String fax3 = (request.getParameter("fax3") != null) ? request.getParameter("fax3") : "";
String fax = fax1+fax2+fax3;
ArrayList<String> phoneArray = new ArrayList<String>();
String addr1 = (request.getParameter("addr1") != null) ? request.getParameter("addr1") : "";
String addr2 = (request.getParameter("addr2") != null) ? request.getParameter("addr2") : "";
String city = (request.getParameter("city") != null) ? request.getParameter("city") : "";
String state = (request.getParameter("state") != null) ? request.getParameter("state") : "";
String zip = (request.getParameter("zip") != null) ? request.getParameter("zip") : "";

User userObj = new User(sc.getActingAs());
Contact contact = new Contact(sc.getEditingSiteId(),"billing");
String expmonth = (request.getParameter("expmonth") != null) ? request.getParameter("expmonth") : "";
String expyear = (request.getParameter("expyear") != null) ? request.getParameter("expyear") : "";
String cvv = (request.getParameter("cvv") != null) ? request.getParameter("cvv") : "";
String cardnumber = (request.getParameter("cardnumber") != null) ? request.getParameter("cardnumber") : "";
String country = (request.getParameter("country") != null) ? request.getParameter("country") : "US";
String amount_posted = (request.getParameter("amount") != null) ? request.getParameter("amount") : "0.00";
String submit_cc = "";

String post_payment = (request.getParameter("post-payment") != null) ? request.getParameter("post-payment") : ""; 
String change_cc_card= (request.getParameter("change-cc-card") != null) ? request.getParameter("change-cc-card") : ""; 

String statusMsg = "";
boolean transaction_result = false;
boolean result = false;
String transaction_message = "";

String next_payment = GenUtils.prettyFormat(bt.calcNextPayment(sc.getEditingSiteId()));
String subscriptionId = contact.getBrainTreeSubscriptionId();
String customerId = contact.getBrainTreeCustomerId();
String defaultCCNumber = bt.getDefaultCCNumber(customerId);

// for directory members
String memberCharge = "N/A";
String memberBillingCycle = "";
	
if (isLocalDirectoryMember) {
	Module mm = new Module(directoryDomain);
	String monthlyMemberCharge = mm.getMonthlyMemberCharge();
	String yearlyMemberCharge = mm.getYearlyMemberCharge();
	if (!monthlyMemberCharge.equals("N/A")) {
		memberCharge = monthlyMemberCharge;	
		memberBillingCycle = " / Month";
	} else if (!yearlyMemberCharge.equals("N/A")) {
		memberCharge = yearlyMemberCharge;
		memberBillingCycle = " / Year";
	}		
	String memberModuleId = mm.getModuleId();
}
/*
if (!post_payment.equals("")) {
	// we are using subscriptions now in braintree
	
	if (!amount_posted.equals("") && !cardnumber.equals("") && cardnumber.length() >= 15 ) {
		subscriptionId = bt.addBrainTreeSubscription(sc.getEditingSiteId(), account_plan, cvv, cardnumber, expmonth, expyear);
		next_payment = bt.calcNextPayment(sc.getEditingSiteId());
		if (!subscriptionId.equals("")) {
			defaultCCNumber = bt.getDefaultCCNumber(customerId);
			isFreeTrial = false;
			isTrialExpired = false;
		}
	} else if (amount_posted.equals("")) {
		transaction_message = "Please enter the amount to charge.";
	} else if (cardnumber.equals("")) {
		transaction_message = "Please enter a credit card number.";
	} else if (cardnumber.length() <15) {
		transaction_message = "Please enter a valid credit card number.";
	}
}
*/
boolean cardUpdated = false;
if (!change_cc_card.equals("")) {
    if (!cvv.equals("") && !cardnumber.equals("") && cardnumber.length() >= 15 ) {
		cardUpdated = bt.updateVault(customerId, cvv, cardnumber, expmonth, expyear);
		if (cardUpdated) defaultCCNumber = bt.getDefaultCCNumber(customerId);
	} else if (cvv.equals("")) {
			transaction_message = "Please enter the 3-digit code on the back of the card.";
	} else if (cardnumber.equals("")) {
			transaction_message = "Please enter a credit card number.";
	} else if (cardnumber.length() <15) {
			transaction_message = "Please enter a valid credit card number.";
	}
}

if(request.getParameter("account-information-save") != null){
	contact.setEmail(login);
	contact.saveContact();
	String userId = userObj.getUserId();
	// change password for user
	if (newPasswordSubmitted.equals(newPasswordSubmittedConfirm) && !newPasswordSubmitted.equals("")) {
	   result = userObj.changePassword(userId, newPasswordSubmitted);
	}
	statusMsg = "<p>Changes saved.</p>";
	if (!sc.getActingAs().equals(login) && !login.equals("")) {
		result = userObj.changeUserName(userId, login);
		if (result) {
			out.print("<script type='text/javascript'>\n");
			out.print("document.location='../error/userlogout.jsp';\n");
			out.print("</script>\n");
		} else {
			statusMsg = "<p>The new username you selected was already taken, but your other changes were saved.<p>";
		}	
	}
	// include the paypal submission
	if (!submit_cc.equals("")) { %>
<jsp:include page="payInclude.jsp" >
<jsp:param name="first_name" value="<%=first_name%>" />
<jsp:param name="last_name" value="<%=last_name%>" />
<jsp:param name="city" value="<%=city%>" />
<jsp:param name="state" value="<%=state%>" />
<jsp:param name="zip" value="<%=zip%>" />
<jsp:param name="address1" value="<%=addr1%>" />
<jsp:param name="address2" value="<%=addr2%>" />
<jsp:param name="country" value="<%=country%>" />
<jsp:param name="expmonth" value="<%=expmonth%>" />
<jsp:param name="expyear" value="<%=expyear%>" />
<jsp:param name="cardnumber" value="<%=cardnumber%>" />
<jsp:param name="cvv" value="<%=cvv%>" />
<jsp:param name="submit_cc" value="true" />
</jsp:include>
	<% } %>               
<% }
if (request.getParameter("billing-information-save") != null) {
	contact.setFirstName(first_name);
	contact.setLastName(last_name);
	contact.setTel(phone);
	contact.setFax(fax);
	contact.setAddr1(addr1);
	contact.setAddr2(addr2);
	contact.setCity(city);
	contact.setState(state);
	contact.setZip(zip);
	contact.saveContact();
}
if (request.getParameter("payment-save") != null) {
	if(request.getParameter("account_plan") != null){
		boolean planresult =  site2.saveSiteAccountPlan(sc.getEditingSiteId(), request.getParameter("account_plan"));
		if (!planresult){
			out.print("<script>alert('Too many properties to downgrade.');</script>");
		} else {
			current_account_plan =  request.getParameter("account_plan");	
		}
	}
} %>
<script type="text/javascript" src="js/email_validation.js" ></script>
<script type="text/javascript">

	function loadPage(list) {
		location.href=list.options[list.selectedIndex].value
	}
	$(document).ready(function(){

		<% if (subscriptionId.equals("")) { %>
			$("#subscribe-button").show();
			$("#change-card-button").hide();
		<% } else { %>
			$("#subscribe-button").hide();
			$("#change-card-button").show();
		<% } %>

		$("#account-tabs").tabs();
		$("#account-tabs").show();
		$("input[type=password]").val("");
		$("#account-information-button").click(function() {
			var login = $("#login").val();
			var error = false;
			if (!validateEmail(login)) {
				$(".statusMsg").show();
				$(".statusMsg").html("<p>Please enter a valid email address.</p>");
				$(".statusMsg").fadeOut(4000);
				error = true;
			}
			if($("#newPass").val() != $("#confirmNewPass").val()){
				$(".statusMsg").show();
				$(".statusMsg").html("<p>Please retype your new password and the confirmation as they do not match.</p>");
				$(".statusMsg").fadeOut(4000);
				error = true;
			}
			if(!error){
				$("#account-information-save").val('true');
				$("#account-information").submit();
			}
		});
		$("#billing-information-button").click(function() {
			$("#billing-information-save").val('true');
			$("#billing-information").submit();
		});
		$("#payment-button").click(function() {
		//	$("#post-payment").val('true');
		//		$("#payment-save").val('true');
		//		$("#payment-information").submit();

				var siteId = "<%=editingSiteId%>";
				var cardNumber = $("#cardnumber").val();
				var cvv = $("#cvv").val();
				var expMonth = $("#expmonth").val();
				var expYear = $("#expyear").val();
				var accountPlan = $("#account_plan").val();

				//alert(siteId);
				//alert(cardNumber);
				//alert(cvv);
				//alert(expMonth);
				//alert(expYear);
				//alert(accountPlan);
				
				$("#process-msg").html("<p><strong>Processing ....</strong><p>");

			$.post("services/processPayment.jsp", {'site_id':siteId, 
								'card_number':cardNumber,
								'cvv':cvv,
								'exp_month':expMonth,
								'exp_year':expYear,
								'account_plan':accountPlan }, function(data) {

					var responseObj = eval('(' + data + ')');
					var status = responseObj['status'];
					var processMsg = responseObj['msg'];
					if (status == 1) {
						var nextPayment = responseObj['next_payment'];
						var defaultCCNumber = responseObj['default_cc_number'];
						$("#cardnumber").val(defaultCCNumber);
						$("#cvv").val('');
						$("#next-payment").html(nextPayment);
						// hide the make a payment button and show the change card button
			                        $("#subscribe-button").hide();
       			                 	$("#change-card-button").show();
						// refresh the document
						$("#process-msg").html("<p><strong>" + processMsg + "</strong></p>");
						$("#process-msg").fadeOut(5000, function() {
							document.location = "account.jsp";
						});
					} else {
						$("#process-msg").html("<p><strong>" + processMsg + "</strong></p>");
					}
			});

		});
		$("#change-cc-button").click(function() {
			$("#change-cc-card").val('true');
			$("#payment-information").submit();
		});
		$("#stateSelect").val("<%=contact.getState()%>");	
	});
</script>
<div class="container">
<%
String trialMessage = "";
String trialStyle = "";
// print a message saying the account is expired if it is
// only show if braintree is enabled
if (use_braintree) {
	if (isTrialExpired) {
		trialMessage = "Your Free Trial has expired.  You will be unable to make any changes to your site until you make a payment.\n";
		trialStyle = "error";
	} else if (isFreeTrial) {
		trialStyle = "highlight";
		long timeRemaining = Env.TRIAL_PERIOD_DAYS - accountAge;
		if (timeRemaining > 1) {
			trialMessage = "<strong>" + timeRemaining + "</strong> days left in your Free Trial.  You will be unable to make any changes to your site once your Free Trial expires.\n";
		} else {
			trialMessage = "<strong>" + timeRemaining + "</strong> day left in your Free Trial.  You will be unable to make any changes to your site once your Free Trial expires.\n";
		}
	}
} %>
	<div>
		<h2 class="title">Account Management</h2>
	</div>
	<div class="statusMsg"></div>
	<div class="transactionMsg"><%=transaction_message%></div>
	<div id="account-tabs">
		<ul>
			<li><a href="#tab-1">Account Information</a></li>	
			<li><a href="#tab-2">Billing Information</a></li>	
			<% // only including account check and cc info (for local directory members) 
			if (!isLocalDirectory && use_braintree) { %>
			<li><a href="#tab-3">Make A Payment</a></li>
			<% } %>
			<div class="clear"></div>				
		</ul>
		<div id="tab-1">
			<h2>Account Information</h2>
			<form id="account-information" name="account-information" method="post" action="account.jsp" >
				<p class="status">Account Status: <%=active%><% if (isFreeTrial) { %> / Free Trial <% } %></p>
				<% if (!trialMessage.equals("")){ %>
				<div class="ui-widget" style="margin-bottom: 20px;">
					<div class="ui-state-<%=trialStyle%> ui-corner-all" style="padding: 0 .7em;"> 
						<h4>
							<span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em; margin-top:16px;"></span>
							<%=trialMessage%>
						</h4>
					</div>
				</div>
				<% } %>
				<label for="login">Email Address<br />
				<span class="small">Note: If changed, you will be logged out and will need to login using your new email.</span>
				<input type="text" id="login" name="login" value="<%=userObj.getUserName()%>" /></label>
				<label for="newPass" id="new-password-label">New Password
				<input type="password" name="new-password" id="newPass" /></label>
				<label for="confirmNewPass" id="new-password-repeat-label">New Password Repeat
				<input type="password" name="new-password-repeat" id="confirmNewPass" /></label>
				<div class="clear"></div>
				<input type="hidden" name="account-information-save" value="" />
				<a id="account-information-button" name="account-information-button" class="btn_big_180_green">Save</a>
			</form>
		</div>
		<div id="tab-2">
			<h2>Billing Information</h2>
			<form id="billing-information" name="billing-information" method="post" action="account.jsp" >
				<label for="firstname" id="firstname-label">First Name
				<input type="text" size="20" name="firstname" id="firstname" value="<%=contact.getFirstName()%>" /></label>
				<label for="lastname" id="lastname-label">Last Name
				<input type="text" size="20" name="lastname" id="lastname" value="<%=contact.getLastName()%>" /></label>
				<label for="addr1" id="addr1-label">Address
				<input type="text" size="20" name="addr1" id="addr1" value="<%=contact.getAddr1()%>" /></label>
				<label for="addr2" id="addr2-label">Address 2
				<input type="text" size="20" name="addr2" id="addr2" value="<%=contact.getAddr2()%>" /></label>
				<label for="city" id="city-label">City
				<input type="text" size="20" name="city" id="city" value="<%=contact.getCity()%>" /></label>
				<label for="stateSelect" id="state-label">State
				<select name="state" id="stateSelect">
					<%=States.outputStateSelect("long",contact.getState())%>
				</select></label>
				<label for="zip" id="zip-label">Zip Code
				<input type="text" name="zip" id="zip" size="5" maxlength="5" value="<%=contact.getZip()%>" /></label>
				<%
				//phone number utilities
				String contactTel = contact.getTel();
				String phoneArrayTel1 = "";
				String phoneArrayTel2 = "";
				String phoneArrayTel3 = "";
				if (!contactTel.equals("")) {
					phoneArray = GenUtils.splitPhone(contactTel);
					if (phoneArray.size() >= 1) phoneArrayTel1 = (phoneArray.get(0) != null) ? phoneArray.get(0) : "";
					if (phoneArray.size() >= 2) phoneArrayTel2 = (phoneArray.get(1) != null) ? phoneArray.get(1) : "";
					if (phoneArray.size() >= 3) phoneArrayTel3 = (phoneArray.get(2) != null) ? phoneArray.get(2) : "";
				}
				%>
				<label for="phone1" id="phone-label">Phone Number
				<div id="phone-container">
				<input type="text" size="3" maxlength="3" class="phone3" name="phone1" id="phone1" value="<%=phoneArrayTel1%>" />
				<input type="text" size="3" maxlength="3" class="phone3" name="phone2" value="<%=phoneArrayTel2%>" />
				<input type="text" size="4" maxlength="4" class="phone4" name="phone3" value="<%=phoneArrayTel3%>" />
				</div></label>
				<div class="clear"></div>
				<input type="hidden" name="billing-information-save" value="" />
				<a id="billing-information-button" name="billing-information-button" class="btn_big_180_green">Save</a>
			</form>
		</div>
		<% // only including account check and cc info (for local directory members) 
		if (!isLocalDirectory && use_braintree) { %>
		<div id="tab-3">
			<h2>Make A Payment</h2>
			<form id="payment-information" name="payment-information" method="post" action="account.jsp" >
				<% if (sc.checkIsLocalDirectoryMember(sc.getEditingSiteId())){ %>
				<p><b>Current Plan:</b> $<%=memberCharge%><%=memberBillingCycle%></p>
				<input type="hidden" name="payment-save" id="payment-save" value="" />
					<% if (!memberCharge.equals("N/A")) { %>
				<input type='hidden' name='amount' value='<%=memberCharge%>' />	
					<% } %>
				<% } else { %>
				<label for="account_plan" id="account_plan-label">Current Plan:
				<select name="account_plan" id="account_plan">
				<%
				boolean downgrade_allowed = false;
				String account_plan_monthly_price = "";
				String amount_due = "";
				String min_amount_due = "";
				boolean min_amount_selected = false;
				account_plan_rs = MysqlQuery.runQuery(account_plan_sql);
				while (account_plan_rs.next()) {
					String account_plan_id = (account_plan_rs.getString("id") != null) ? account_plan_rs.getString("id") : "";
					downgrade_allowed = site.isDowngradeAllowed(editingSiteId, account_plan_id);
					account_plan_monthly_price = (account_plan_rs.getString("monthly_price") != null) ? account_plan_rs.getString("monthly_price") : "";
					String account_plan_max_num_properties = (account_plan_rs.getString("max_num_properties") != null) ? account_plan_rs.getString("max_num_properties") : "";
					String account_plan_description = (account_plan_rs.getString("description") != null) ? account_plan_rs.getString("description") : "";
					String account_plan_selected = (current_account_plan.equals(account_plan_id)) ? " selected" : "";
					if (current_account_plan.equals(account_plan_id)) {
						amount_due = account_plan_monthly_price;
					}
					if (downgrade_allowed && !min_amount_selected) {
						min_amount_due = account_plan_monthly_price;
						min_amount_selected = true;
					}		
					if (downgrade_allowed) { %>
					<option value="<%=account_plan_id%>"<%=account_plan_selected%>><%=account_plan_description%> - <%=account_plan_monthly_price%></option>
					<% } else { %>
					<option value="<%=account_plan_id%>"<%=account_plan_selected%> disabled><%=account_plan_description%> - <%=account_plan_monthly_price%></option>
					<% }  
				} %>
				</select></label>
				<% if (amount_due.equals("")){
					amount_due = min_amount_due; 
				} %>
				<input type='hidden' name='amount' id='amount' value='<%=amount_due%>' />			
				<input type='hidden' name='post-payment' id='post-payment' value='' />
				<input type='hidden' name='change-cc-card' id='change-cc-card' value='' />
				<% } %>
			
				<label for="cardnumber" id="cardnumber-label">Card Number
				<input size="20" onkeyup="javascript:this.value=this.value.replace(/[^0-9*]/g, '');" name="cardnumber" id="cardnumber" type="text" value="<%=defaultCCNumber%>"></label>
				<img id="cards-logo" src="assets/images/cards-logo.jpg" />
				<label for="cvv" id="cvv-label">CVV
				<input size="3" onkeyup="javascript:this.value=this.value.replace(/[^0-9]/g, '');" name="cvv" id="cvv" type="text"></label>
				<img id="cvv-logo" src="assets/images/cvv-logo.gif" />
				<label for="expmonth" id="expmonth-label">Month
				<select name="expmonth" id="expmonth">
					<option selected="selected" value="01">01</option>
					<option value="02">02</option>
					<option value="03">03</option>
					<option value="04">04</option>
					<option value="05">05</option>
					<option value="06">06</option>
					<option value="07">07</option>
					<option value="08">08</option>
					<option value="09">09</option>
					<option value="10">10</option>
					<option value="11">11</option>
					<option value="12">12</option>
				</select></label>
				<label for="expyear" id="expyear-label">Year
				<select name="expyear" id="expyear">
					<option selected="selected" value="2011">2011</option>
					<option value="2012">2012</option>
					<option value="2013">2013</option>
					<option value="2014">2014</option>
					<option value="2015">2015</option>
					<option value="2016">2016</option>
					<option value="2017">2017</option>
					<option value="2018">2018</option>
					<option value="2019">2019</option>
					<option value="2020">2020</option>
				</select></label>
				<div class="clear"></div>
				<p><strong>Next Payment Due On: </strong><span id="next-payment"><%=next_payment%></span></p>
				<div id='process-msg'></div>
				<div id="subscribe-button">
					<a id="payment-button" name="payment-button" class="btn_big_180_green">Subscribe</a> 
					<p>Note: When you click 'Subscribe', your account will be set up for recurring monthly payments via BrainTree.</p>
				</div>
				<div id="change-card-button">
					<a id="change-cc-button" name="change-cc-button" class="btn_big_180_green">Change Card</a>
					<p>Note: Your account is currently set up for recurring payments via BrainTree.</p>
					<p><a href='payments.jsp'>View Payment History</a></p>
				</div>
				<a href="https://www.braintreegateway.com/merchants/xd4t3rcszp867b7c/verified" target="_blank">
					<img src="https://braintree-badges.s3.amazonaws.com/06.png" border="0"/>
				</a>
			</form>
		</div>		
		<% } %>
	</div>
<%@include file="widgets/knowledgebase_widget.jsp" %>
<%@include file="footer.jsp" %>

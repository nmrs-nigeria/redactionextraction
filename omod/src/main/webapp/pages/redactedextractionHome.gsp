<% ui.decorateWith("appui", "standardEmrPage") %>

<%= ui.resourceLinks() %>

<div class="row wrapper white-bg page-heading" style="display: flex; justify-content: center; align-items: center;">
    <h4 style="text-align: center;">
        NDR Export for Redacted
    </h4>
</div>


<div class="container" style="padding-top: 10px;">
    <div style="margin-left: 32%; width: 40%; height: 50%; background-color: #00463f; border-radius: 10px; ">
        <br/> <br/>
        <div>
            <label for="custom" style="font-weight: bold; color: white; cursor: pointer;">
                <input style="background-color: #E8F0FE; border-radius: 10px; margin-top: 15px; cursor: pointer" type="checkbox" id="custom" name="custom" value="custom" onclick="checkBoxCheck()">
                Custom
            </label>
            <br id="br1">
            <label id="customlabels" style="display: none;">Comma Separated Patient ART Identifiers</label>
            <br id="br1">
            <input style="background-color: #E8F0FE; width: 85%; height: 45px; border-radius: 10px; margin-top: 15px; padding-left: 18px; padding-right: 10px; display: none" type="text" id="identifiers"  name="identifiers"><br id="br2">
            <br/>
            <label id="lblfrom" for="startdate" style="color: white; margin-left: 50px;">Encounter Start Date</label><br id="br4">
            <input style="background-color: #E8F0FE; margin-left: 52px;margin-bottom: 15px; width: 70%; height: 45px; border-radius: 25px; margin-top: 15px;" name="startdate" id="startdate" type="date" required="required"/><br>
            <label id="lblto" for="enddate" style="color: white; margin-left: 50px;">Encounter End Date</label><br id="br6">
            <input style="background-color: #E8F0FE; margin-left: 52px;margin-bottom: 15px; width: 70%; height: 45px; border-radius: 25px; margin-top: 15px;" name="enddate" id="enddate" type="date" required="required"/><br>
            <input style="background-color: #E8F0FE; margin-left: 52px; width: 70%; height: 45px; border-radius: 25px; margin-top: 15px" type="button" value="Export" onclick="getStart()" class="btn btn-primary" />
        </div>
        <br/><br/>
    </div>
    <br/>
    <div class="table-responsive">
        <table class="table table-striped table-bordered  table-hover" id="tb_commtester">
            <thead>
            <tr>
                <th>${ ui.message("File Name") }</th>
                <th>${ ui.message("Date Started") }</th>
                <th>${ ui.message("Date Completed") }</th>
                <th>${ ui.message("Total No. of Patients") }</th>
                <th>${ ui.message("Actions") }</th>
            </tr>

            </thead>
            <tbody id="TableBody">

            </tbody>
        </table>
    </div>
    <div id="gen-wait" class="dialog" style="display: none; position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%); z-index: 9999;">
        <div class="row">
            <div class="col-md-3 col-xs-3 offset-2" >
                <img src="../moduleResources/redactedextraction/images/Sa7X.gif" alt="Loading Gif"  style="width:100px">
            </div>
        </div>

        <div>
            <div class="col-md-7 col-xs-7 " style="text-align:center;">
                <h1>Please wait, operation in progress...</h1>
            </div>
        </div>
    </div>
</div>

<script>
    var jq = jQuery;

    function getStart() {
        // Show 'gen-wait' element when processing starts.
        jq('#gen-wait').show();

        console.log("Started Job");
        var startdate = document.getElementById("startdate").value;
        var enddate = document.getElementById("enddate").value;
        var patientidentifiers = document.getElementById("identifiers").value;

        // Check if either startdate or enddate is empty
        if(startdate === "" || enddate === ""){
            alert("Please provide both start date and end date");
            // Hide 'gen-wait' element since there's no processing happening
            jq('#gen-wait').hide();
            return;
        }

        jq.ajax({
            url: "${ ui.actionLink("redactedextraction", "redactedextractionHome", "extractRedacted") }",
            dataType: "json",
            data:{
                'startdate':startdate,
                'enddate':enddate,
                'patientidentifiers':patientidentifiers
            },
            success: function (response) {
                // Hide 'gen-wait' element when a response is received.
                jq('#gen-wait').hide();
                console.log(response);
                var res = JSON.parse(response);
                if(res === "No record found") {
                    alert(res);
                } else {
                    console.log(res);
                    jq('#TableBody')
                        .append("<tr>" +
                            "<td>" + res[0] + "</td>" +
                            "<td>" + res[1] + "</td>" +
                            "<td>" + res[1] + "</td>" +
                            "<td>" + res[2] + "</td>" +
                            "<td><a href='" + res[3] + "' download>Download</a></td>" +
                            "</tr>");
                }
            },
            error:function(xhr){
                // Hide 'gen-wait' element if an error occurs.
                jq('#gen-wait').hide();
                console.log(xhr);
            }
        });
    }

</script>


<script>
    var today = new Date();
    var dd = String(today.getDate()).padStart(2, '0');
    var mm = String(today.getMonth() + 1).padStart(2, '0'); //January is 0!
    var yyyy = today.getFullYear();

    today = yyyy + '-' + mm + '-' + dd;
    document.getElementById("startdate").setAttribute("max", today);
    document.getElementById("enddate").setAttribute("max", today);

    function checkBoxCheck()
    {
        const checkBox = document.getElementById("custom");
        if (checkBox.checked === true){
            document.getElementById('identifiers').style.display =  'inline';
            document.getElementById('customlabels').style.display='block';

        }else{
            document.getElementById('identifiers').style.display =  'none';
            document.getElementById('customlabels').style.display='none';
        }

    }
</script>
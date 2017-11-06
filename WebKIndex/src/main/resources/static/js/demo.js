
//variables
var elderlycaretips_urls=[
    "http://www.elderlycaretips.info/products.php",
   "http://www.elderlycaretips.info/news.php",
    "http://www.elderlycaretips.info/index.php",
    "http://www.elderlycaretips.info/holidays-for-the-elderly.php",
    "http://www.elderlycaretips.info/holiday-insurance-for-the-elderly.php",
    "http://www.elderlycaretips.info/Taking Seniors In Your Care On Outings.php",
    "http://www.elderlycaretips.info/The Basics Of Elderly Personal Hygiene.php",
    "http://www.elderlycaretips.info/The Best Activities To Stimulate Mentally Ill Seniors.php",
    "http://www.elderlycaretips.info/The Importance Of Medication When Caring For Seniors.php",
    "http://www.elderlycaretips.info/A Beginners Guide To Retirement Homes.php",
    "http://www.elderlycaretips.info/A Quick Guide To Hospice Care.php",
    "http://www.elderlycaretips.info/Caring For Elderly Pressure Sores.php",
    "http://www.elderlycaretips.info/Caring For The Elderly And Incontinence.php",
    "http://www.elderlycaretips.info/Develop Your Skills To Care For The Elderly.php",
    "http://www.elderlycaretips.info/Effective Caring For Your Elderly Relatives.php",
    "http://www.elderlycaretips.info/Essential Information About Being Power Of Attorney.php",
    "http://www.elderlycaretips.info/Tracking Devices For Elderly People.php",
    "http://www.elderlycaretips.info/Senior Illnesses And What They Mean To You.php",
    "http://www.elderlycaretips.info/Washing An Elderly Person.php",
    "http://www.elderlycaretips.info/An Unusual Method To Stop Alzheimers  Patients Wandering Off.php",
    "http://www.elderlycaretips.info/The Distress To The Elderly Caused By Leaving A Pet .php",
    "http://www.elderlycaretips.info/Promoting And Maintaining Independence Whilst Caring For The Elderly.php",
    "http://www.elderlycaretips.info/caring_for_the_elderly_at_home.php",
    "http://www.elderlycaretips.info/elderlycaretips_privacy_policy.php",
    "http://www.elderlycaretips.info/rember_a_new_drug_for_alzheimers.php",
    "http://www.elderlycaretips.info/Time_to_think_about_a_care_home.php",
    "http://www.elderlycaretips.info/assisted_living_facilities.php",
    "http://www.elderlycaretips.info/home_health_care.php",
    "http://www.elderlycaretips.info/Types Of Stair Lift.php",
    "http://www.elderlycaretips.info/Straight Stair Lifts.php",
    "http://www.elderlycaretips.info/Curved Stair Lifts.php",
    "http://www.elderlycaretips.info/used_stair_lifts.php",
    "http://www.elderlycaretips.info/stair-lift-prices.php",
    "http://www.elderlycaretips.info/night-time-incontinence-elderly.php",
    "http://www.elderlycaretips.info/elderly-care-tips-book.php",
    "http://www.elderlycaretips.info/crime-precautions-outside-home.php",
    "http://www.elderlycaretips.info/crime-precautions-inside-home.php"
]




//on page load
$(function(){

    //add a click listener to "initial-url-list" buttons
    $("#initial-url-list > button").on("click", function(){
        //dynamic variable name
        var table = window[$(this).attr("table")];
        $("#crawled-url-list").empty();
        hide_eveything_after_step(1);
        $.each(table, function(key, url){
            $("#extracted-urls").show();
            $("#crawled-url-list").append(" <button type=\"button\" class=\"list-group-item\" onclick=\"extract_content(\'"+url+"\')\">" + url + "</button>");
        });
    });

    $('[data-toggle="tooltip"]').tooltip();

});

function hide_eveything_after_step(step){
    if (step < 2){
        $("#extracted-urls").hide();
    }
    if (step < 3){
        $("#extracted-text").empty();
        $("#second-row").hide();
    }
    if (step < 4){
        $("#third-row").hide();
    }
    $("#forth-row").hide();

}

function extract_content(url){
    //display components
    hide_eveything_after_step(2);
    $("#second-row").show();
    $("#crawled-url-list button").css("background-color","white");
    $("#crawled-url-list button:contains('"+url+"')").css("background-color","aliceblue");

    $("#extracted-text").html("<img id=\"gears-gif\" src=\"images/gears.gif\"/>");
    $("#content-frame").attr("src",url);
    /*var myIframe = document.getElementById('content-frame');
    myIframe.onload = function(){
        myIframe.contentWindow.scrollTo(50,50);
    };*/

    $.ajax({
        url: "extractWebsite?url=" + encodeURIComponent(url),
        success: function(response){
            $("#extracted-text").empty();
            $("#extracted-text").html(response.replace(/(?:\r\n|\r|\n)/g, '<br/>'));
        },
        error: function(){
             $("#extracted-text").empty();
             $("#extracted-text").html("An error occurred");
        }
    });

    //scroll to the bottom of the page
    $('html, body').animate({
       scrollTop: $('footer').offset().top
       //scrollTop: $('#your-id').offset().top
       //scrollTop: $('.your-class').offset().top
    }, 'slow');
}

function extract_concepts_nes(){

    $("#ne-label>h3").html(" Named Entities ");
    $("#concept-label>h3").html(" Concept ");

    $("#extracted-concepts").empty();
    $("#extracted-concepts").html("<tr><td><img id=\"gears-gif\" src=\"images/gears.gif\"/></td></tr>");
    $("#extracted-ne").empty();
    $("#extracted-ne").html("<tr><td><img id=\"gears-gif\" src=\"images/gears.gif\"/></td></tr>");

    $("#third-row").show();

    var content = $("#extracted-text").html();

    content = content.replace(new RegExp("<br>" , 'ig'), ". ").replace(new RegExp("\\*" , 'ig'), " ");
    var text = $("<div>" + content + "</div>").text()
    $.ajax({
        url: "extractConceptsNe?text="+text,
        success: function(response){

            $("#extracted-concepts").empty();
            $("#extracted-ne").empty();

            try{
                var resp = JSON.parse(response);

                var foundConcept=false;
                $.each(resp.concepts, function (key, array) {
                     var conceptString ="";
                    var prefix = "";
                    $.each(array, function (pos, value) {
                        conceptString += prefix + value;
                        prefix = ", ";

                        //annotate text
                        var prevText = $("#extracted-text").html();
                        var find = new RegExp("<br>" + value +"| " + value+"|&nbsp;" + value, 'ig');
                        var replace = " <span class=\"concept-span\" data-toggle=\"tooltip\" title=\"" + key + "\">"+value+"</span>";
                        $("#extracted-text").html($("#extracted-text").html().replace(find,replace));
                        //if no replacement took place
                        if (prevText == $("#extracted-text").html()){
                            var array = value.split(" ");
                            for(i=0;i<array.length;i++){
                                var val = array[i];
                                if ((val != " ") && (val.length>2)){
                                    var find = new RegExp("<br>" + val +"| " + val+"|&nbsp;" + val, 'ig');
                                    var replace = " <span class=\"concept-span\" data-toggle=\"tooltip\" title=\"" + key + "\">"+val+"</span>";
                                    $("#extracted-text").html($("#extracted-text").html().replace(find,replace));
                                }
                            }
                        }

                        foundConcept=true
                    });
                    $("#extracted-concepts").append("<tr><td>"+key+"</td><td>"+conceptString+"</td></tr>");

                });

                if (foundConcept){
                    $("#concept-label>h3").html("<span class=\"concept-span\"> Concept </span>");
                }

                var foundNE = false;
                $.each(resp.ne, function (key, array) {

                    var neString ="";
                    var prefix = "";
                    $.each(array, function (pos, value) {
                        neString += prefix + value;
                        prefix = ", ";

                        //annotate text
                        var prevText = $("#extracted-text").html();
                        var find = new RegExp("<br>" + value +"| " + value+"|&nbsp;" + value, 'ig');
                        var replace = " <span class=\"ne-span\" data-toggle=\"tooltip\" title=\"" + key + "\">"+value+"</span>";
                        $("#extracted-text").html($("#extracted-text").html().replace(find,replace));

                        //if no replacement took place
                        if (prevText == $("#extracted-text").html()){
                            var array = value.split(" ");
                            for(i=0;i<array.length;i++){
                                var val = array[i];
                                if ((val != " ") && (val.length>2)){
                                    var find = new RegExp("<br>" + val +"| " + val+"|&nbsp;" + val, 'ig');
                                    var replace = " <span class=\"ne-span\" data-toggle=\"tooltip\" title=\"" + key + "\">"+val+"</span>";
                                    $("#extracted-text").html($("#extracted-text").html().replace(find,replace));
                                }
                            }
                        }
                        foundNE=true;
                    });
                    $("#extracted-ne").append("<tr><td>"+key+"</td><td>"+neString+"</td></tr>");

                });

                if (foundNE){
                    $("#ne-label>h3").html("<span class=\"ne-span\"> Named Entities </span>");
                }

                $('[data-toggle="tooltip"]').tooltip();

                $("#forth-row").show();
            }
            catch(error){
                $("#extracted-concepts").html("<tr><td colspan=\"2\">An error occurred</td></tr>");
                $("#extracted-ne").html("<tr><td colspan=\"2\">An error occurred</td></tr>");
                $("#forth-row").show();
            }
        },
        error: function (){
                $("#extracted-concepts").html("<tr><td colspan=\"2\">An error occurred</td></tr>");
                $("#extracted-ne").html("<tr><td colspan=\"2\">An error occurred</td></tr>");
                $("#forth-row").show();
        }

    });

    //scroll to the bottom of the page
    $('html, body').animate({
       scrollTop: $('footer').offset().top
       //scrollTop: $('#your-id').offset().top
       //scrollTop: $('.your-class').offset().top
    }, 'slow');
}

window.jQuery || document.write('<script src="js/jquery-1.11.3.min.js"><\/script>')

$(document).ready(function() {
      var q1 =
        ` PREFIX : <http://kristina-project.eu/ontologies/re#>
 PREFIX owl: <http://www.w3.org/2002/07/owl#>
 PREFIX dbpedia: <http://dbpedia.org/page/>
 SELECT distinct ?treatment
 WHERE {
   ?s a :Fever.
   ?t :isAdministeredFor ?s.
   ?t :text ?treatment .
 }`;

      var q2 =
        ` PREFIX : <http://kristina-project.eu/ontologies/re#>
 PREFIX owl: <http://www.w3.org/2002/07/owl#>
 PREFIX dbpedia: <http://dbpedia.org/page/>
 SELECT distinct ?indication
 WHERE {
  ?back_pain a :Problem.
  ?back_pain owl:sameAs dbpedia:Back_pain .
  ?back_pain :isIndicatedBy [:text ?indication].
 }`;

 var q3 = 
 `PREFIX : <http://kristina-project.eu/ontologies/re#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX dbpedia: <http://dbpedia.org/page/>
SELECT distinct ?problem
WHERE {
  ?back_pain a :Problem.
  ?back_pain owl:sameAs dbpedia:Back_pain.
  ?p :indicates ?back_pain.
  ?p :text ?problem.
}
 `;

      var btn_clicked = 'btn1';
      $('#btn1').click(function() {
        $("#queryText").val(q1);
        btn_clicked = 'btn1';
      });

      $('#btn2').click(function() {
        $("#queryText").val(q2);
        btn_clicked = 'btn2';
      });

      $('#btn3').click(function() {
        $("#queryText").val(q3);
        btn_clicked = 'btn3';
      });

      $('#clear').click(function() {
        $("#queryText").val("");
        $("#queryResult").val("");
      });

      $('#run').click(function() {
        var result = '';
        if(btn_clicked === 'btn1'){
            result = runQuery1($("#queryText").val());
        } else if(btn_clicked === 'btn2'){
          result = runQuery2($("#queryText").val());
        } else if(btn_clicked === 'btn3'){
          result = runQuery3($("#queryText").val());
        }


      });



      function runQuery1(queryText) {
        $.ajax({
          url: 'http://160.40.50.196:8084/graphdb-workbench-free/repositories/re',
          type: 'GET',
          crossDomain: true,
          headers: {
            Accept: 'application/json'
          },
          data: {
            query: queryText
          }
        }).done(function(data) {
          var results = data.results.bindings;
          var build = '';
          $.each(results, function(index, item) {
            console.log(item.treatment.value.trim());
            build += item.treatment.value.trim() + '\n';
          });
          $('#queryResult').val(build);
        }).fail(function() {
          console.log("error");
        }).always(function() {
          console.log("complete");
          return true;
        });
      }


    function runQuery2(queryText) {
      $.ajax({
        url: 'http://160.40.50.196:8084/graphdb-workbench-free/repositories/re',
        type: 'GET',
        crossDomain: true,
        headers: {
          Accept: 'application/json'
        },
        data: {
          query: queryText
        }
      }).done(function(data) {
        var results = data.results.bindings;
        var build = '';
        $.each(results, function(index, item) {
          console.log(item.indication.value.trim());
          build += item.indication.value.trim() + '\n';
        });
        $('#queryResult').val(build);
      }).fail(function() {
        console.log("error");
      }).always(function() {
        console.log("complete");
        return true;
      });
    }


    function runQuery3(queryText) {
      $.ajax({
        url: 'http://160.40.50.196:8084/graphdb-workbench-free/repositories/re',
        type: 'GET',
        crossDomain: true,
        headers: {
          Accept: 'application/json'
        },
        data: {
          query: queryText
        }
      }).done(function(data) {
        var results = data.results.bindings;
        var build = '';
        $.each(results, function(index, item) {
          console.log(item.problem.value.trim());
          build += item.problem.value.trim() + '\n';
        });
        $('#queryResult').val(build);
      }).fail(function() {
        console.log("error");
      }).always(function() {
        console.log("complete");
        return true;
      });
    }

    
  });
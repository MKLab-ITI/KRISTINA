

    $(document).ready(function(){
        //$('#one-sentence-table').DataTable({"paging":   false});

        $.ajax({
            url: "/webapquery",
            dataType: 'json',
            success: function(response){
                $('#query-selection').select2({
                    data: response
                });
            }
        });

        $('#query-selection').change(function() {
           makeQuery( $('#query-selection').val());
        });

    });


    function makeQuery(id){
        $.ajax({
            url: "WebAPGetQuery?qid="+id,
            success : function(resp){

                if ($("#combined-table>tbody").find("tr").size()>0){
                    $("#combined-table").DataTable().destroy();
                    $("#all-segments-table").DataTable().destroy();
                }
                $("#combined-table>tbody").html("");
                $("#all-segments-table>tbody").html("");
                jQuery.each(resp, function(IndexType, array) {
                    jQuery.each(array, function() {
                        if ((IndexType == "OneSentence") || (IndexType == "TwoSentences") || (IndexType == "ThreeSentences")){
                            $("#combined-table").find('tbody')
                                .append($('<tr>')
                                    .append($('<td>').text(this.type))
                                    .append($('<td>') .text(this.docID + ":" + this.sentID) )
                                    .append($('<td>').text(this.relevance) )
                                    .append($('<td>').text(this.text))
                                    .append($('<td>').text(this.vsmScore))
                                    .append($('<td>').text(this.lmdscore))
                                    .append($('<td>').text(this.lmjmscore))
                                );
                        }
                        else if (IndexType == "AllSegmentation"){
                            $("#all-segments-table").find('tbody')
                                .append($('<tr>')
                                    .append($('<td>').text(this.type))
                                    .append($('<td>') .text(this.docID + ":" + this.sentID) )
                                    .append($('<td>').text(this.relevance) )
                                    .append($('<td>').text(this.text))
                                    .append($('<td>').text(this.vsmScore))
                                    .append($('<td>').text(this.lmdscore))
                                    .append($('<td>').text(this.lmjmscore))
                                );
                        }
                    });
                });
                $('#combined-table').DataTable({"paging":   true});
                $('#all-segments-table').DataTable({"paging":   true});

            }
        });
    }
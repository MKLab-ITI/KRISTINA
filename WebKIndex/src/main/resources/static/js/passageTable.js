$(document).ready(function(){
    $('#all-segmentation-table').DataTable({"paging":   false, "order": [[ 5, "desc" ]]});
    $('#one-sentence-table').DataTable({"paging":   false, "order": [[ 3, "desc" ]]});
    $('#two-sentences-table').DataTable({"paging":   false, "order": [[ 3, "desc" ]]});
    $('#three-sentences-table').DataTable({"paging":   false, "order": [[ 3, "desc" ]]});
    $('#paragraph-table').DataTable({"paging":   false, "order": [[ 3, "desc" ]]});
    $('#document-table').DataTable({"paging":   false, "order": [[ 3, "desc" ]]});
});
$().ready(function() {
	$("button#submit").click(function() {
		var elems = document.getElementsByTagName("input");
		$.each(elems, function() {
			if (this.checked) {
				$.ajax({url:"/GetDataFromURL?url="+this.value.replace('&', 'AND'), async: false});
			}
		})
	});
})
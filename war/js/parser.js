$().ready(function() {
	window.countSubmitted = 0;
	$("button#submit").click(function() {
		$("#loading").css("display", "inline");
		var elems = document.getElementsByTagName("input");
		$.each(elems, function() {
			if (this.checked) {
				window.countSubmitted++;
				$.ajax({url:"/GetDataFromURL?url="+this.value.replace('&', 'AND').replace('AMP', '&'), async: false, success:function() {
					$.get("/updatecount");
					window.countSubmitted--;
					if (window.countSubmitted === 0) {
						$("#loading").hide();
					}
				}, error: function() {
					$.get("/updatecount");
					window.countSubmitted--;
					if (window.countSubmitted === 0) {
						$("#loading").hide();
					}
					alert("Error");
				}});
			}
		});
		alert("Complete");
	});
})
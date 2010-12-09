<!DOCTYPE html>
<html>
<head>
	<meta http-equiv='content-type' content='text/html; charset=UTF-8'> 
	<link type='text/css' rel='stylesheet' href='index.css'>
	<link type='text/css' rel='stylesheet' href='http://bits.wikimedia.org/skins-1.5/common/diff.css?283-16'>
	<title>ClueBot NG Report Interface</title>
</head>
<body>
	<div id="top"> 
		<div class="floatleft"><h1 id="title">ClueBot NG Report Interface</h1></div> 
		<div class="floatright"><h1 id="subtitle">// <?PHP $page->writeHeader(); ?></h1></div> 
	</div> 

	<div class="hline"></div> 

	<div class="floatleft"> 
		<div id="left"> 
			<h2>Navigation</h2> 
			<div id="menu">
				<?PHP $page->writeNavigation(); ?>
			</div> 
		</div> 
	</div> 
	<div id="content">
		<?PHP $page->writeContent(); ?>
	</div>

<!--
	<div id='header'>
		
	</div>
	<div style="clear: both; border-bottom: 1px solid #d3e1f9;"></div>
	<div id='navigationContainer'>
		<div id='navigation'>
			
		</div>
	</div>
	<div id='content'>
		
	</div>
-->
</body>
</html>
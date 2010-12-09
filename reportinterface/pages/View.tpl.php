<table class="reporttable">
	<tr>
		<th>ID:</th>
		<td><?PHP echo $this->row[ 'id' ]; ?></td>
	</tr>
	<tr>
		<th>User:</th>
		<td><?PHP echo $this->row[ 'user' ]; ?></td>
	</tr>
	<tr>
		<th>Article:</th>
		<td><?PHP echo $this->row[ 'article' ]; ?></td>
	</tr>
	<tr>
		<th>Diff:</th>
		<td class="diffborder">
			<?PHP echo file_get_contents( 'http://en.wikipedia.org/w/index.php?diffonly=1&action=render&diff=' . urlencode( $this->row[ 'new_id' ] ) ); ?>
		</td>
	</tr>
	<tr>
		<th>Reason:</th>
		<td><?PHP echo $this->row[ 'reason' ]; ?></td>
	</tr>
</table>

<table class="reporttable reportertable">
	<tr><th colspan=2 style="text-align: center">Reporter Information</th></tr>
	<tr>
		<th>Reporter:</th>
		<td>
			<?PHP echo htmlentities( $this->data[ 'username' ] ); ?>
			<?PHP if( $this->data[ 'anonymous' ] ) { ?>
				<small>(anonymous)</small>
			<?PHP } ?>
		</td>
	</tr>
	<tr>
		<th>Date:</th>
		<td><?PHP echo date( 'l, \t\h\e jS \o\f F Y \a\t h:i:s A', $this->data[ 'timestamp' ] ); ?></td>
	</tr>
	<tr>
		<th>Status:</th>
		<td>
			<?PHP echo $this->data[ 'status' ]; ?>
			<?PHP if( isAdmin() ) { ?>
				(
				<a href="?page=View&id=<?PHP echo $this->row[ 'id' ]; ?>&status=0">Reported</a> &middot;
				<a href="?page=View&id=<?PHP echo $this->row[ 'id' ]; ?>&status=1">Invalid</a> &middot;
				<a href="?page=View&id=<?PHP echo $this->row[ 'id' ]; ?>&status=2">Defer to Review Interface</a> &middot;
				<a href="?page=View&id=<?PHP echo $this->row[ 'id' ]; ?>&status=3">Bug</a> &middot;
				<a href="?page=View&id=<?PHP echo $this->row[ 'id' ]; ?>&status=4">Resolved</a>
				)
			<?PHP } ?>
		</td>
	</tr>
</table>

<?PHP foreach( $this->data[ 'comments' ] as $comment ) { ?>
	<div class="comment" id="cmt<?PHP echo $comment[ 'id' ]; ?>">
		<div class="commentheader">
			<span class="commentdate">
				<?PHP echo date( 'l, \t\h\e jS \o\f F Y \a\t h:i:s A', $comment[ 'timestamp' ] ); ?>
			</span>
			<span class="commentid">
				<?PHP if( isSAdmin() ) { ?>
					<a href="?page=View&id=<?PHP echo $this->row[ 'id' ]; ?>&deletecomment=<?PHP echo $comment[ 'id' ]; ?>">(x)</a> &middot; 
				<?PHP } ?>
				<a href="#cmt<?PHP echo $comment[ 'id' ]; ?>">#<?PHP echo $comment[ 'id' ]; ?></a>
			</span>
		</div>


		<div class="commentdata">
			<div class="commentuser">
				<?PHP echo htmlentities( $comment[ 'username' ] ); ?>
				<?PHP if( $comment[ 'anonymous' ] ) { ?>
					<small>(anonymous)</small>
				<?PHP } ?>
				<?PHP if( $comment[ 'sadmin' ] ) { ?>
					<small>(super admin)</small>
				<?PHP } else if( $comment[ 'admin' ] ) { ?>
					<small>(admin)</small>
				<?PHP } ?>
			</div>
			<div class="commentdata2">
				<div class="commentbody">
					<?PHP echo '<p>' . str_replace( "\n\n", '</p><p>', str_replace( "\r", '', htmlentities( $comment[ 'comment' ] ) ) ) . '</p>'; ?>
				</div>
			</div>
		</div>
	</div>
<?PHP } ?>

<?PHP
	$user = 'Anonymous';
	if( isset( $_SESSION[ 'username' ] ) )
		$user = $_SESSION[ 'username' ];
?>
<form method="post">
	<table class="reporttable">
		<tr>
			<th>Username:</th>
			<td><input type="text" name="user" value="<?PHP echo $user; ?>" /></td>
		</tr>
		<tr>
			<th>Comment:</th>
			<td><textarea name="comment" cols=80 rows=25></textarea></td>
		</tr>
		<tr>
			<td colspan=2><input type="submit" name="submit" value="Post comment" /></td>
		</tr>
	</table>
</form>
Apache HTTPD artifactory docker registry using wildcard DNS/SSL
===============================================================

This is a SSL configuration for apache httpd with multiple docker repositories using a DNS wildcard.  It assumes artifactory and 
apache httpd are running on the same machine. You can use this artifactory.conf as a starting point for your installation.

This takes more configuration of the environment it's running on than the other versions.  To use this you need
to create a wildcard DNS entry for the machine.  If you don't want to use DNS you can use the /etc/hosts file, however
it can't do wildcards s you'll need to have an entry for each repository you're planning on running:

<pre>
/etc/hosts
....
127.0.0.1 docker-prod-local.localhost docker-prod-local2.localhost
127.0.0.1 docker-dev-local.localhost docker-dev-local2.localhost
</pre>

Also you'll need to edit the artifactory.conf file, it's set up for \*.localhost, however if you want anything other
you'll need to change the artifactory.conf file and use the wildcard-ssl.sh script to make an appropriate self-signed 
certificate.

To run the demo:

docker build --rm --tag art_apache_https_docker .
docker run -d --name art_apache_https_docker art_apache_https_docker

#To find the IP of the newly running container use:
docker inspect --format '{{ .NetworkSettings.IPAddress }}' art_apache_https_docker

__Note__
This assumes a file named local.sh that contains something like:

<pre>
sed -i '/mirrorlist/d;s/#baseurl/baseurl/;s~mirror.centos.org~artifactory-us.jfrog.info/artifactory~' /etc/yum.repos.d/CentOS-*.repo
rm /etc/yum.repos.d/Artifactory*
</pre>

This edits the CentOS yum repositories to point at your local artifactory.  Change the artifactory-us.jfrog.info to your
local artifactory instance.

We supply a blank one here so the Dockerfile works.


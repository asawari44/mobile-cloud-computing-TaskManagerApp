""" Sets up Trucaller API """
from setuptools import setup, find_packages

VERSION = '0.1.01'

setup(
    name='mcc-backend-api',
    version=VERSION,
    description='mcc-backend api',
    long_description='',
    classifiers=[
        'Development Status :: 3 - Alpha',
        'Environment :: Console',
        'Intended Audience :: Developers',
        'License :: Other/Proprietary License',
        'Natural Language :: English',
        'Programming Language :: Python :: 3'
    ],
    keywords='',
    author='Aashir Javed',
    author_email='aashir.javed@aalto.fi',
    url='',
    license='',
    packages=find_packages(),
    install_requires=['flask-restful', 'pyrebase', 'weasyprint'],
    include_package_data=True,
    zip_safe=False
)
from setuptools import setup

setup(name='ustream',
      version='0.1',
      description='Simple distributed map/reduce for SQL and machine learning',
      url='https://github.com/burakbayramli/ustream',
      author='Burak Bayramli',
      author_email='dd@post.com',
      license='MIT',
      packages=['ustream'],
      install_requires=[
          'smart_open','zmq'
      ],
      zip_safe=False)
